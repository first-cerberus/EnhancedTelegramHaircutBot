package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HaircutBot extends TelegramLongPollingBot {
    private final Map<String, String> clientToDate = new HashMap<>();
    private final Map<String, String> clientToBarber = new HashMap<>();
    private final Map<Long, String> userInputStage = new HashMap<>();
    private final Map<String, String> tempDateUser = new HashMap<>();
    public static Set<Long> adminIds;
    private final String botUsername;
    private final String botToken;
    private final String bookingsFile = "bookings.json";
    private final ObjectMapper objectMapper;
    private String firstName;


    public HaircutBot(String botUsername, String botToken, Set<Long> adminIds) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        HaircutBot.adminIds = adminIds;
        objectMapper = new ObjectMapper();
        configureObjectMapper(objectMapper);
        //loadBookings();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            firstName = update.getMessage().getFrom().getFirstName();
            long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();
            String messageText = update.getMessage().getText();
            if (messageText.equals("/start") || messageText.equals("В головне меню")) {
                showMainMenu(chatId);
            } else if (messageText.equals("Записатись на стрижку")) {
                showBarberSelection(chatId);
            } else if (messageText.equals("Показати всі записи")) {

            } else if (messageText.equals("Видалити свій запис")) {

            } else if (messageText.equals("Адмін функціонал")) {
                if (isAdmin(userId)) showAdminMenu(chatId);
                else sendTextMessage(chatId, "Не клацай сюди, ти ж не адмін :)");
            } else if (messageText.equals("Різа") || messageText.equals("Іванов") || messageText.equals("Дубов")) {
                if (List.of("Різа", "Іванов", "Дубов").contains(messageText)) {
                    clientToBarber.put(firstName, messageText);
                    System.out.println(clientToBarber);
                }
                daySelectionButtons(chatId);
            }
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId(); // Получаем chatId из callback
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            if ("Сьогодні_pressed".equals(callbackData)) {
                clientToDate.put(firstName, "Сьогодні_pressed");
                System.out.println(clientToDate);
                timeSelectionButtons(chatId);
            } else if ("Завтра_pressed".equals(callbackData)) {
                clientToDate.put(firstName, "Завтра_pressed");
                timeSelectionButtons(chatId);
            } else if ("Післязавтра_pressed".equals(callbackData)) {
                clientToDate.put(firstName, "Післязавтра_pressed");
                timeSelectionButtons(chatId);
            } else if ("customDay_pressed".equals(callbackData)) {
                customDate(chatId);
            } else if ("18:00_pressed".equals(callbackData)) {
                String chosenDay = clientToDate.get(firstName);
                if ("Сьогодні_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    String dateToPut = currentDate + " 18:00";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Завтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(1);
                    String dateToPut = nextDay + " 18:00";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Післязавтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(2);
                    String dateToPut = nextDay + " 18:00";
                    clientToDate.put(firstName, dateToPut);
                }
            } else if ("19:00_pressed".equals(callbackData)) {
                String chosenDay = clientToDate.get(firstName);
                if ("Сьогодні_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    String dateToPut = currentDate + " 19:00";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Завтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(1);
                    String dateToPut = nextDay + " 19:00";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Післязавтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(2);
                    String dateToPut = nextDay + " 19:00";
                    clientToDate.put(firstName, dateToPut);
                }
            } else if ("19:25_pressed".equals(callbackData)) {
                String chosenDay = clientToDate.get(firstName);
                if ("Сьогодні_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    String dateToPut = currentDate + " 19:25";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Завтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(1);
                    String dateToPut = nextDay + " 19:25";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Післязавтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(2);
                    String dateToPut = nextDay + " 19:25";
                    clientToDate.put(firstName, dateToPut);
                }
            } else if ("19:45_pressed".equals(callbackData)) {
                String chosenDay = clientToDate.get(firstName);
                if ("Сьогодні_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    String dateToPut = currentDate + " 19:45";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Завтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(1);
                    String dateToPut = nextDay + " 19:45";
                    clientToDate.put(firstName, dateToPut);
                } else if ("Післязавтра_pressed".equals(chosenDay)) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextDay = currentDate.plusDays(2);
                    String dateToPut = nextDay + " 19:45";
                    clientToDate.put(firstName, dateToPut);
                }
            }
            try {
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void daySelectionButtons(long chatId) {
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
            e.printStackTrace();
        }
    }

    public void timeSelectionButtons(long chatId) {
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
        fourthOption.setText("Інший час");
        fourthOption.setCallbackData("customTime_pressed");
        row2.add(fourthOption);

        keyboard.add(row1);
        keyboard.add(row2);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Записатись на стрижку", "Видалити свій запис"),
                List.of("Показати всі записи", "Адмін функціонал")
        ));
        sendKeyboardMessage(chatId, "Вітаю! Оберіть дію:", keyboardMarkup);
    }

    private void showAdminMenu(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Змінити час запису", "Видалити запис користувача"),
                List.of("Додати запис за іншого", "В головне меню")
        ));
        sendKeyboardMessage(chatId, "Виберіть дію:", keyboardMarkup);
    }

    private void showBarberSelection(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = createKeyboard(List.of(
                List.of("Різа", "Іванов", "Дубов"),
                List.of("В головне меню")
        ));
        sendKeyboardMessage(chatId, "Оберіть барбера:", keyboardMarkup);
    }

    private void addAppointment() {

    }
    private void customDate(long chatId){
        sendTextMessage(chatId,"Введіть дату в форматі: 2025-02-01 12:00");

    }
    private void promptForBooking(long chatId) {
        sendTextMessage(chatId, "Введіть дату (формат: 2024-12-21 15:00)");
    }

    private boolean isAdmin(long userId) {
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
            e.printStackTrace();
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String BookingConfirmationText(LocalDateTime dateTime) {
        return "✅ Ви успішно записались на: " + formatDateTime(dateTime) + "\n" +
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


    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
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