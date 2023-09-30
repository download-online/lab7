package ru.jefremov.prog.server.managers;


import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.models.Event;
import ru.jefremov.prog.common.models.Ticket;
import ru.jefremov.prog.common.models.validation.AbstractTicketValidator;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.exceptions.database.DatabaseException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс для хранилища, обеспечивающего ограниченный доступ к коллекции.
 * Обеспечивает корректность хранящихся данных и предоставляет удобные средства для управления коллекцией.
 */
public class Storage {
    private Set<Ticket> collection;
    private final AbstractTicketValidator ticketValidator;
    private final ServerAdministrator administrator;
    private LocalDate initDate;

    /**
     * Конструктор для хранилища
     * @param administrator администратор
     * @param ticketValidator валидатор билетов
     */
    public Storage(ServerAdministrator administrator, AbstractTicketValidator ticketValidator) {
        if (administrator==null) throw new IllegalArgumentException("Administrator must not be null");
        this.administrator = administrator;
        if (ticketValidator==null) throw new IllegalArgumentException("Ticket validator must not be null");
        this.ticketValidator = ticketValidator;
        this.collection = Collections.synchronizedSet(new LinkedHashSet<>());
        initDate = LocalDate.now();
    }

    /**
     * Геттер для копии хранящейся коллекции.
     * @return копия коллекции
     */
    public LinkedHashSet<Ticket> getCollectionCopy() {
        return new LinkedHashSet<>(collection);
    }

    /**
     * Геттер для администратора
     * @return администратор
     */
    public ServerAdministrator getAdministrator() {
        return administrator;
    }

    /**
     * Загрузить коллекцию в хранилище. Отвергает неподходящие коллекции.
     * @param collection коллекция
     * @return подошла ли она для загрузки
     */
    public boolean loadCollection(LinkedHashSet<Ticket> collection) {
        if (collection==null) {
            this.collection = Collections.synchronizedSet(new LinkedHashSet<>());
            initDate = LocalDate.now();
            return true;
        }
        if (checkCollection(collection)) {
            this.collection = Collections.synchronizedSet(collection);
            initDate = LocalDate.now();
            return true;
        }
        return false;
    }

    /**
     * Проверяет коллекцию на соответствие требованиям валидатора элементов.
     * @param collection коллекция
     * @return прошла ли коллекция проверку, или нет
     */
    public boolean checkCollection(LinkedHashSet<Ticket> collection) {
        if (collection==null) return false;
        return collection.stream().allMatch(ticketValidator::checkTicket);
    }

    /**
     * Выявляет проблемы в коллекции при её проверке
     * @param collection коллекция
     * @return Комментарий, если есть проблемы, или null, если коллекция успешно прошла проверку.
     */
    public String reviewCollection(LinkedHashSet<Ticket> collection) {
        if (collection==null) {
            return null;
        }
        Optional<String> verdict = collection.stream().map(ticketValidator::reviewTicket).filter(Objects::nonNull).findAny();
        return verdict.orElse(null);
    }

    /**
     * Добавление билета. Отвергает неподходящие элементы.
     * @param ticket билет
     * @return Подошёл ли он для добавления, или нет.
     */
    public synchronized boolean addTicket(Ticket ticket, UserInfo info) {
        Optional<Ticket> checked = Stream.of(ticket).filter(ticketValidator::checkTicket)
                .findFirst();
        if (checked.isEmpty()) return false;
        boolean success = false;
        try {
            success = administrator.databaseManager.insertTicket(ticket,info);
        } catch (SQLException | DatabaseException ignored) {
        }
        if (success) {
            return collection.add(checked.get());
        }
        return false;
    }

    /**
     * Получение билета по id
     * @param id id
     * @return билет с соответствующим идентификатором
     */
    private synchronized Ticket getById(int id) {
        Optional<Ticket> ticket = collection.stream().filter(ticket1 -> ticket1.getId()==id).findFirst();
        return ticket.orElse(null);
    }

    /**
     * Проверка, присутствует ли в коллекции билет с нужным id
     * @param id id
     * @return результат проверки
     */
    public synchronized boolean hasId(int id) {
        return (getById(id)!=null);
    }

    /**
     * Обновляет билет с нужным id, копируя поля у другого билета. Другой билет может быть отвергнут.
     * @param id id
     * @param other другой билет
     * @return Состоялось ли обновление (подошёл ли другой билет, или нет)
     */
    public synchronized boolean updateById(int id, Ticket other, UserInfo info) {
        Ticket ticket = getById(id);
        if (Stream.of(other).noneMatch(ticketValidator::checkTicket)) return false;
        if (ticket==null) return false;
        boolean success = false;
        try {
            success = administrator.databaseManager.update(other,id,info);
            if (success) {
                collection.remove(ticket);
                ticket.update(other);
                return collection.add(ticket);
            }
        } catch (Exception e) {
            Printer.println(e.getMessage());
        }
        return success;
    }

    /**
     * Удаляет билет по id
     * @param id id
     * @return изменилась ли коллекция
     */
    public synchronized boolean removeById(int id, UserInfo info) {
        Optional<Ticket> matching = Stream.of(id).map(this::getById).filter(Objects::nonNull).findFirst();
        if (matching.isEmpty()) return false;
        try {
            List<Integer> ids = administrator.databaseManager.getOwnedTickets(info);
            administrator.databaseManager.remove(id,info);
            if (ids.contains(id)) return collection.remove(matching.get());
            else return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Очищает коллекцию
     */
    public synchronized void clear(UserInfo info) {
        try {
            List<Integer> ids = administrator.databaseManager.getOwnedTickets(info);
            administrator.databaseManager.clear(ids, info);
            List<Ticket> collected =collection.stream().filter(ticket -> ids.contains(ticket.getId())).peek(Printer::print).toList();
            collection.removeAll(collected);
        } catch (Exception ignored) {
        }
    }

    /**
     * Получение размера коллекции
     * @return размер коллекции
     */
    public int size() {
        return collection.size();
    }

    /**
     * Выводит информацию о коллекции.
     */
    public String printInfo() {
        return (collection.getClass().getName()+" collection with "+size()+" elements.\n"+
                "Initialisation date: "+ initDate+"\nThe collection is linked to a database.");
    }

    /**
     * Добавляет билет, если он меньше всех элементов в коллекции и проходит проверку.
     * @param ticket билет
     * @return добавлен ли билет (прошёл ли он проверку)
     */
    public synchronized boolean addIfMin(Ticket ticket, UserInfo info) {
        Optional<Ticket> checked = Stream.of(ticket).filter(ticket1 -> collection.isEmpty() || Collections.min(collection).compareTo(ticket1)>0).findFirst();
        return (checked.isPresent() && addTicket(checked.get(),info));
    }

    /**
     * Удаляет все элементы в коллекции, меньшие, чем заданный
     * @param ticket заданный билет
     * @return количество удалённых элементов
     */
    public synchronized int removeLower(Ticket ticket, UserInfo info) {
        int sizeBefore = collection.size();
        Collection<Ticket> collected = collection.stream().filter(ticket1 -> ticket1.compareTo(ticket)<0).toList();
        List<Integer> ids = collected.stream().map(Ticket::getId).collect(Collectors.toList());
        try {
            administrator.databaseManager.clear(ids,info);
        } catch (Exception e) {
            return 0;
        }
        collected.forEach(collection::remove);

        int sizeAfter = collection.size();
        return sizeAfter-sizeBefore;
    }

    /**
     * Выводит элементы коллекции, чей комментарий начинается с заданной подстроки
     * @param comment заданная подстрока
     */
    public synchronized List<Ticket> printFilterStartsWithComment(String comment) {
        if (!ticketValidator.checkComment(comment)) return null;
        return collection.stream().filter(ticket->ticket.getComment()!=null && ticket.getComment().startsWith(comment)).collect(Collectors.toList());
    }

    /**
     * Выводит все элементы коллекции, чьё событие меньше, чем заданное
     * @param event заданное событие
     */
    public synchronized List<Ticket> printFilterLessThanEvent(Event event) {
        if (!ticketValidator.checkEvent(event)) return null;
        return collection.stream().filter(ticket->ticket.getEvent().compareTo(event)<0).collect(Collectors.toList());
    }

    /**
     * Выводит коллекцию в порядке убывания
     * @return есть ли в коллекции элементы.
     */
    public synchronized List<Ticket> printDescending() {
        if (collection.isEmpty()) return null;
        return new ArrayList<>(collection);
    }
}
