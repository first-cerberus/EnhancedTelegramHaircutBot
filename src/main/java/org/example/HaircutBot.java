package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HaircutBot extends TelegramLongPollingBot {
    private Map<String, TreeMap<LocalDateTime, String>> bookings = new HashMap<>();
    private final String botUsername;
    private final String botToken;
    public static Set<Long> adminIds;
    private final String bookingsFile = "bookings.dat";
    private boolean isWaitingForDate = false;
    private boolean isWaitingForTime = false;
    private String selectedBarber;
    private String time;
    private String firstName;
    private int messageId;
    private Long userId;
    private long chatId;

    public HaircutBot(String botUsername, String botToken, Set<Long> adminIds) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        HaircutBot.adminIds = adminIds;
        ObjectMapper objectMapper = new ObjectMapper();
        configureObjectMapper(objectMapper);
        loadDataFromFile();
    }

    @Override
    public void onUpdateReceived(Update update) {
        checkForOldAppointments();
        SendSticker sendSticker = new SendSticker();
        if (update.hasMessage() && update.getMessage().hasText()) {
            firstName = update.getMessage().getFrom().getFirstName();
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            String messageText = update.getMessage().getText();

            if (messageText.equals("/start") || messageText.equals("В головне меню")) {
                showMainMenu();
            } else if (messageText.equals("Записатись на стрижку")) {
                checkIfHasAppointment();
            } else if (messageText.equals("Показати всі записи")) {
                showAllAppointments();
            } else if (messageText.equals("Видалити свій запис")) {
                deleteMyAppointment();
            } else if (messageText.equals("Адмін функціонал")) {
                if (isAdmin()) showAdminMenu();
                else sendTextMessage(chatId, "Не клацай сюди, ти ж не адмін :)");
            } else if (messageText.equals("Видалити запис користувача")) {
                showDeleteButtons(chatId);
            } else if (messageText.equals("Різа") || messageText.equals("Іванов") || messageText.equals("Дубов")) {
                selectedBarber = messageText;
                daySelectionButtons();
            } else if (isWaitingForDate) {
                customDate(update);
            } else if (isWaitingForTime) {
                customTime(update);
            } else {
                sendSticker.setChatId(chatId);
                sendSticker.setSticker(new InputFile("CAACAgIAAxkBAAEMBTpnmTLWwQYp3ckdlAAB3tuIL7av7doAAlsBAAJOm2QCn76adxydxHI2BA"));
                sendTextMessage(chatId, "якийсь булшииит, код не выкупает что надо делать, делай все по инструкции, клацай «Записатись на стрижку» и погнал :)");
            }
            try {
                execute(sendSticker);
            } catch (TelegramApiException e) {
                System.out.println("Стикер с Пепе не смог отправиться, попросите разраба пофиксить это :(");
            }
        }
        if (update.hasCallbackQuery()) {
            messageId = update.getCallbackQuery().getMessage().getMessageId();
            String callbackData = update.getCallbackQuery().getData();
            if (callbackData.startsWith("delete_")) {
                handleDeleteAppointment(update);
            }
            long chatId = update.getCallbackQuery().getMessage().getChatId(); // Получаем chatId из callback
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            switch (callbackData) {
                case "Сьогодні_pressed" -> {
                    time = "Сьогодні_pressed";
                    updateKeyboard(chatId, messageId, timeSelectionButtons(chatId));
                }
                case "Завтра_pressed" -> {
                    time = "Завтра_pressed";
                    updateKeyboard(chatId, messageId, timeSelectionButtons(chatId));
                }
                case "Післязавтра_pressed" -> {
                    time = "Післязавтра_pressed";
                    updateKeyboard(chatId, messageId, timeSelectionButtons(chatId));
                }
                case "customDay_pressed" -> {
                    sendTextMessage(chatId, "Введіть дату в форматі: yyyy-MM-dd HH:mm");
                    isWaitingForDate = true;
                    //метод в hasMessage блоке
                }
                case "18:00_pressed" -> {
                    String chosenDay = time;
                    switch (chosenDay) {
                        case "Сьогодні_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            time = currentDate + " 18:00";
                            pushData(chatId);
                        }
                        case "Завтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(1);
                            time = nextDay + " 18:00";
                            pushData(chatId);
                        }
                        case "Післязавтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(2);
                            time = nextDay + " 18:00";
                            pushData(chatId);
                        }
                        case null, default -> {
                        }
                    }
                }
                case "19:00_pressed" -> {
                    String chosenDay = time;
                    switch (chosenDay) {
                        case "Сьогодні_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            time = currentDate + " 19:00";
                            pushData(chatId);
                        }
                        case "Завтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(1);
                            time = nextDay + " 19:00";
                            pushData(chatId);
                        }
                        case "Післязавтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(2);
                            time = nextDay + " 19:00";
                            pushData(chatId);
                        }
                        case null, default -> {
                        }
                    }
                }
                case "19:25_pressed" -> {
                    String chosenDay = time;
                    switch (chosenDay) {
                        case "Сьогодні_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            time = currentDate + " 19:25";
                            pushData(chatId);
                        }
                        case "Завтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(1);
                            time = nextDay + " 19:25";
                            pushData(chatId);
                        }
                        case "Післязавтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(2);
                            time = nextDay + " 19:25";
                            pushData(chatId);
                        }
                        case null, default -> {
                        }
                    }
                }
                case "19:45_pressed" -> {
                    String chosenDay = time;
                    switch (chosenDay) {
                        case "Сьогодні_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            time = currentDate + " 19:45";
                            pushData(chatId);
                        }
                        case "Завтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(1);
                            time = nextDay + " 19:45";
                            pushData(chatId);
                        }
                        case "Післязавтра_pressed" -> {
                            LocalDate currentDate = LocalDate.now();
                            LocalDate nextDay = currentDate.plusDays(2);
                            time = nextDay + " 19:45";
                            pushData(chatId);
                        }
                        case null, default -> {
                        }
                    }
                }
                case "customTime_pressed" -> {
                    sendTextMessage(chatId, "Введіть час в форматі: 13:45");
                    isWaitingForTime = true;
                    //ловим время в hasMessage блоке, сверху
                }
                default -> {
                }
            }
            try {
                execute(response);
            } catch (TelegramApiException e) {
                System.out.println("Помилка в обробці логіки :(");
            }
        }
    }

    private void pushData(long chatId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        List<String> barbersWithOneMachine = Arrays.asList("Іванов", "Різа");
        TreeMap<LocalDateTime, String> dateToClient = bookings.computeIfAbsent(selectedBarber, _ -> new TreeMap<>());
        // Если выбран не "Дубов", проверяем коллизию
        if (!"Дубов".equals(selectedBarber)) {
            boolean conflict = bookings.entrySet().stream()
                    .filter(entry -> barbersWithOneMachine.contains(entry.getKey()))  // Фильтруем только Иванова и Ризу
                    .flatMap(entry -> entry.getValue().keySet().stream()) // Берем только время из записей
                    .anyMatch(existingDate -> {
                        // Проверяем, если разница между временами записи меньше 15 минут
                        long minutesBetween = Math.abs(Duration.between(dateTime, existingDate).toMinutes());
                        return minutesBetween < 15; // Возвращаем true, если коллизия
                    });

            if (isPastDate(dateTime)) {
                sendTextMessage(chatId, "Якщо в тебе є машина часу, то чому б і не записатися на минулу дату? Якщо б в мене була машина часу, то я б тоді не поступав сюди \uD83D\uDE05");
                deleteDaySelectionButtons(chatId, messageId);
                daySelectionButtons();
                return;
            }

            if (conflict) {
                sendTextMessage(chatId, "Інтервал між записами 15 хвилин! Оберіть інший час! :)");
                deleteDaySelectionButtons(chatId, messageId);
                daySelectionButtons();
                return;
            }
        }
        // Додаємо запис без перевірки або після перевірки (якщо барбер не "Дубов")
        dateToClient.put(dateTime, firstName);
        sendTextMessage(chatId, BookingConfirmationText());
        notifyBarber(selectedBarber);
        saveDataToFile();
    }

    private void customDate(Update update) {
        List<String> barbersWithOneMachine = Arrays.asList("Іванов", "Різа");

        if (update.hasMessage() && update.getMessage().hasText()) {
            String userInput = update.getMessage().getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(userInput, formatter);

            // Получаем TreeMap для выбранного барбера
            TreeMap<LocalDateTime, String> dateToClient = bookings.computeIfAbsent(selectedBarber, _ -> new TreeMap<>());

            // Проверяем записи, если выбран не "Дубов"
            if (!"Дубов".equals(selectedBarber)) {
                boolean conflict = false;

                // Если выбран не "Дубов", проверяем коллизию для Ризы и Иванова вместе
                if (barbersWithOneMachine.contains(selectedBarber)) {
                    // Проверка на коллизию между всеми записями для Иванова и Ризы
                    conflict = bookings.entrySet().stream()
                            .filter(entry -> barbersWithOneMachine.contains(entry.getKey()))  // Фильтруем только Иванова и Ризу
                            .flatMap(entry -> entry.getValue().keySet().stream()) // Берем только время из записей
                            .anyMatch(existingDate -> {
                                // Проверяем, если разница между временами записи меньше 15 минут
                                long minutesBetween = Math.abs(Duration.between(dateTime, existingDate).toMinutes());
                                return minutesBetween < 15; // Возвращаем true, если коллизия
                            });
                }
                // Если это не прошлое время, а есть коллизия, выводим сообщение
                if (isPastDate(dateTime)) {
                    sendTextMessage(chatId, "Якщо в тебе є машина часу, то чому б і не записатися на минулу дату? Якщо б в мене була машина часу, то я б тоді не поступав сюди \uD83D\uDE05");
                    deleteDaySelectionButtons(chatId, messageId);
                    daySelectionButtons();
                    return;
                }

                if (conflict) {
                    sendTextMessage(chatId, "Інтервал між записами 15 хвилин! Якщо не хочеш, щоб комусь щось не достригли, будь ласка, оберіть інший час :)");
                    deleteDaySelectionButtons(chatId, messageId);
                    daySelectionButtons();
                    return;
                }
            }

            // Добавляем запись для Дубова или после проверки для Ризы и Иванова
            dateToClient.put(dateTime, firstName);
            time = userInput;
            sendTextMessage(chatId, BookingConfirmationText());
            notifyBarber(selectedBarber);
            saveDataToFile();
            isWaitingForDate = false;
        }
    }

    private void customTime(Update update) {
        LocalTime parsedTime = null;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userInput = update.getMessage().getText();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                parsedTime = LocalTime.parse(userInput, formatter);
                isWaitingForTime = false;
            } catch (Exception o) {
                sendTextMessage(chatId, "Дороге жабеня, введіть час як тобі велено було...");
                return;
            }
        }
        switch (time) {
            case "Сьогодні_pressed" -> {
                LocalDate currentDate = LocalDate.now();
                time = currentDate + " " + parsedTime;
                pushData(chatId);
            }
            case "Завтра_pressed" -> {
                LocalDate currentDate = LocalDate.now();
                LocalDate nextDay = currentDate.plusDays(1);
                time = nextDay + " " + parsedTime;
                pushData(chatId);
            }
            case "Післязавтра_pressed" -> {
                LocalDate currentDate = LocalDate.now();
                LocalDate nextDay = currentDate.plusDays(2);
                time = nextDay + " " + parsedTime;
                pushData(chatId);
            }
        }
    }

    public void daySelectionButtons() {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Оберіть один із варіантів: ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton todayButton = new InlineKeyboardButton();
        todayButton.setText("Сьогодні");
        todayButton.setCallbackData("Сьогодні_pressed");
        row1.add(todayButton);

        InlineKeyboardButton tomorrowButton = new InlineKeyboardButton();
        tomorrowButton.setText("Завтра");
        tomorrowButton.setCallbackData("Завтра_pressed");
        row1.add(tomorrowButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();//2ой ряд
        InlineKeyboardButton afterTomorrowButton = new InlineKeyboardButton();
        afterTomorrowButton.setText("Післязавтра");
        afterTomorrowButton.setCallbackData("Післязавтра_pressed");
        row2.add(afterTomorrowButton);

        InlineKeyboardButton customButton = new InlineKeyboardButton();
        customButton.setText("Інший день");
        customButton.setCallbackData("customDay_pressed");
        row2.add(customButton);

        keyboard.add(row1);
        keyboard.add(row2);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Кнопочки з вибором дня не відправились :(");
        }
    }

    private void deleteDaySelectionButtons(long chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(null); // Удаляем клавиатуру
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            System.out.println("Кнопочки з вибором дня не видалились :(");
        }
    }

    public InlineKeyboardMarkup timeSelectionButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("⌚ Оберіть час запису:⌚");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton firstOption = new InlineKeyboardButton();
        firstOption.setText("18:00");
        firstOption.setCallbackData("18:00_pressed");
        row1.add(firstOption);

        InlineKeyboardButton secondOption = new InlineKeyboardButton();
        secondOption.setText("19:00");
        secondOption.setCallbackData("19:00_pressed");
        row1.add(secondOption);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton thirdOption = new InlineKeyboardButton();
        thirdOption.setText("19:25");
        thirdOption.setCallbackData("19:25_pressed");
        row2.add(thirdOption);

        InlineKeyboardButton fourthOption = new InlineKeyboardButton();
        fourthOption.setText("19:45");
        fourthOption.setCallbackData("19:45_pressed");
        row2.add(fourthOption);

        InlineKeyboardButton fifthOption = new InlineKeyboardButton();
        fifthOption.setText("Інший час");
        fifthOption.setCallbackData("customTime_pressed");
        row2.add(fifthOption);

        keyboard.add(row1);
        keyboard.add(row2);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return inlineKeyboardMarkup;
    }

    private void updateKeyboard(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);

        if (newKeyboard != null) {
            editMessageReplyMarkup.setReplyMarkup(newKeyboard); // Устанавливаем новую клавиатуру
        }
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            System.out.println("Баба клава не оновилася :( Передайте програмісту, що в нього руки не з того місця...");
        }
    }

    private void showMainMenu() {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Записатись на стрижку", "Видалити свій запис"),
                List.of("Показати всі записи", "Адмін функціонал")
        ));
        sendKeyboardMessage(chatId, randomFactMethod(), keyboardMarkup);
    }

    private String randomFactMethod() {
        String[] randomFact = {
                "Дельфіни сплять половиною мозку, щоб не тонути",
                "Коали сплять до 22 годин на добу через бідну їжу.",
                "Пінгвіни часто формують моногамні пари на все життя.",
                "Слонів неможливо загіпнотизувати через їхню високу чутливість.",
                "Октопуси мають три серця, два для зябер і одне для тіла.",
                "Мурахи можуть не спати по кілька тижнів.",
                "Кити можуть співати на відстані до 800 км під водою.",
                "Зебри народжуються з білою шкірою і чорними смугами.",
                "Кролики не можуть блювати.",
                "Леви сплять до 20 годин на добу.",
                "Корови мають найкращих друзів і страждають від стресу, якщо їх розлучити.",
                "Павуки можуть жити без їжі кілька місяців.",
                "Скорпіони світяться в темряві через спеціальні речовини в їхній шкірі.",
                "Кенгуру не можуть рухатися назад.",
                "Ворони можуть запам'ятовувати людські обличчя і навіть мститися.",
                "Жирафи сплять всього кілька годин на добу.",
                "Тигри можуть бути білими, але не є окремим видом.",
        };
        Random random = new Random();
        int index = random.nextInt(randomFact.length);
        return randomFact[index];
    }

    private void showAdminMenu() {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Додати запис за іншого", "Видалити запис користувача"),
                List.of("*Вакантно*", "В головне меню")
        ));
        sendKeyboardMessage(chatId, "Виберіть дію:", keyboardMarkup);
    }

    private void showBarberSelection() {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Різа", "Іванов", "Дубов"),
                List.of("В головне меню")
        ));
        sendKeyboardMessage(chatId, "Оберіть барбера:", keyboardMarkup);
    }

    private void showAllAppointments() {
        // Проверка на пустоту bookings
        if (bookings.isEmpty()) {
            sendTextMessage(chatId, "Пусто, в жопі виросла капуста...");
            return;
        }
        // Строка для всех записей
        StringBuilder allAppointments = new StringBuilder();

        // Перебираем все записи в bookings
        for (Map.Entry<String, TreeMap<LocalDateTime, String>> entry : bookings.entrySet()) {
            String barberName = entry.getKey(); // Имя барбера
            TreeMap<LocalDateTime, String> appointments = entry.getValue(); // Записи этого барбера

            // Строим строку для каждого барбера
            allAppointments.append(barberName).append(": ").append("\n");

            // Перебираем все записи (клиент - дата)
            for (Map.Entry<LocalDateTime, String> appointment : appointments.entrySet()) {
                LocalDateTime appointmentTime = appointment.getKey(); // Время записи
                String clientName = appointment.getValue(); // Имя клиента

                // Добавляем информацию о клиенте и времени записи в строку
                allAppointments.append("\t\t\t\t").append(clientName).append(" - ")
                        .append(appointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            }

            // Добавляем разделитель между барберами (если нужно)
            allAppointments.append("\n");
        }

        // Отправляем все записи в одном сообщении
        sendTextMessage(chatId, allAppointments.toString());
    }

    private void deleteMyAppointment() {
        boolean appointmentRemoved = false;

        // Перебираем всех барберов
        Iterator<Map.Entry<String, TreeMap<LocalDateTime, String>>> barberIterator = bookings.entrySet().iterator();
        while (barberIterator.hasNext()) {
            Map.Entry<String, TreeMap<LocalDateTime, String>> barberEntry = barberIterator.next();
            //String barberName = barberEntry.getKey();
            TreeMap<LocalDateTime, String> appointments = barberEntry.getValue();

            // Перебираем записи для текущего барбера
            Iterator<Map.Entry<LocalDateTime, String>> appointmentIterator = appointments.entrySet().iterator();
            while (appointmentIterator.hasNext()) {
                Map.Entry<LocalDateTime, String> appointment = appointmentIterator.next();
                String clientName = appointment.getValue(); // Имя клиента

                // Если нашли запись для этого клиента, удаляем её
                if (firstName.equals(clientName)) {
                    appointmentIterator.remove();
                    saveDataToFile();
                    appointmentRemoved = true;
                    break; // Прерываем после удаления, так как клиент может быть только один
                }
            }
            // Если у барбера больше нет записей, удаляем барбера
            if (appointments.isEmpty()) {
                barberIterator.remove();
            }
        }
        // Проверка, был ли удалён клиент
        if (appointmentRemoved) {
            sendTextMessage(chatId, "Ваш запис видалений! Запишись знову, мені бабосікі треба...");
        } else {
            sendTextMessage(chatId, "Ваш запис не знайдений! Можливо варто спочатку записатись, а потім шось клацати!!");
        }
    }

    private boolean isAdmin() {
        return adminIds.contains(userId);
    }

    private boolean isPastDate(LocalDateTime dateTime) {
        return dateTime.isBefore(LocalDateTime.now());
    }

    private ReplyKeyboardMarkup createKeyboard(List<List<String>> rows) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (List<String> row : rows) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.addAll(row);
            keyboardRows.add(keyboardRow);
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void sendKeyboardMessage(long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Клавіатура не відправилась :(");
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String BookingConfirmationText() {
        showMainMenu();
        return "✅ Ви успішно записались на: " + time + "\n" +
                "❗В нас стрижуться по передоплаті:\n" +
                "\uD83D\uDE0DРіза:\n" +
                "          4149499995087812 Privat\n" +
                "          5375411410802206 Monobank\n" +
                "\uD83E\uDD70Ілля:\n" +
                "          4149499995087820 Privat\n" +
                "\uD83D\uDE18ІлляДубов:\n" +
                "          5375235104443930 A-Bank\n" +
                "          4149499990441709 Privat\n" +
                "— Стандартна ціна: 100₴\n" +
                "\uD83D\uDD25 Якщо бажаєте уникнути черги, сплатіть >= 150₴!";
    }

    private void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(bookingsFile))) {
            oos.writeObject(bookings);
            System.out.println("✅ Данные успешно сохранены!");
        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadDataFromFile() {
        File file = new File(bookingsFile);
        if (!file.exists()) {
            System.out.println("📂 Файл с записями отсутствует, создаём новый.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            bookings = (Map<String, TreeMap<LocalDateTime, String>>) ois.readObject();
            System.out.println("✅ Данные успешно загружены!");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void showDeleteButtons(long chatId) {
        if (bookings.isEmpty()) {
            sendTextMessage(chatId, "Пусто, в жопі виросла капуста!");
            return;
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<String, TreeMap<LocalDateTime, String>> entry : bookings.entrySet()) {
            String barber = entry.getKey();
            for (Map.Entry<LocalDateTime, String> appointment : entry.getValue().entrySet()) {
                LocalDateTime dateTime = appointment.getKey();
                String client = appointment.getValue();

                String buttonText = barber + ": " + client + " - " + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String callbackData = "delete_" + barber + "_" + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"));

                InlineKeyboardButton button = new InlineKeyboardButton(buttonText);
                button.setCallbackData(callbackData);

                rows.add(Collections.singletonList(button));
            }
        }

        keyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите запись для удаления:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Кнопочки для удаления записей не отправились :(");
        }
    }

    private void handleDeleteAppointment(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        // Разбираем callbackData
        String[] parts = callbackData.split("_", 3);
        if (parts.length < 3) return;

        String barberName = parts[1]; // Имя барбера
        String dateTimeStr = parts[2].replace("_", " "); // Дата и время в строковом формате

        // Парсим дату и время
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            sendTextMessage(chatId, "Ошибка парсинга даты");
            return;
        }
        // Проверяем, есть ли записи у этого барбера
        if (bookings.containsKey(barberName)) {
            TreeMap<LocalDateTime, String> appointments = bookings.get(barberName);
            // Удаляем запись
            if (appointments.remove(dateTime) != null) {
                sendTextMessage(chatId, "✅ Запис успішно видалено!");
                saveDataToFile();
                // Если больше нет записей, удаляем барбера из списка
                if (appointments.isEmpty()) {
                    bookings.remove(barberName);
                }
                // Обновляем клавиатуру
                showDeleteButtons(chatId);
            } else {
                sendTextMessage(chatId, "⛔ Такого запису не знайдено!");
            }
        } else {
            sendTextMessage(chatId, "⛔ У барбера нема записів!");
        }
    }

    private void checkForOldAppointments() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, TreeMap<LocalDateTime, String>> entry : bookings.entrySet()) {
            TreeMap<LocalDateTime, String> dateToClient = entry.getValue();

            // Если запись старше текущего времени, удаляем её
            dateToClient.entrySet().removeIf(booking -> booking.getKey().isBefore(now));
        }
    }

    private void checkIfHasAppointment() {
        boolean alreadyBooked = bookings.values().stream()
                .anyMatch(treeMap -> treeMap.containsValue(firstName));
        if (alreadyBooked) {
            sendTextMessage(chatId, "У вас вже є запис! Спочатку скасуйте поточний запис перед створенням нового.");
            showMainMenu();
        } else {
            showBarberSelection();
        }
    }
    private void notifyBarber(String selectedBarber){
        Map<String, Long> barberChatIds = Map.of(
                "Різа", 1514302273L, // Замените на фактические ID барберов
                "Іванов", 799128809L,
                "Дубов", 670778441L
        );
        Long barberChatId = barberChatIds.get(selectedBarber);
        if (barberChatId == null) {
            System.out.println("ID барбера для відправки повідомлення " + selectedBarber + " не знайдено.");
            return;
        }
        String message = "Новий клієнт захотів знову виглядати неймовірно:\n" +
                "Клієнт: " + firstName + "\n" +
                "Дата: " + time;

        sendTextMessage(barberChatId, message);
    }

    private void configureObjectMapper(ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());  // Регистрация сериализатора
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());  // Регистрация десериализатора
        objectMapper.registerModule(module);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}