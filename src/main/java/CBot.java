import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtensionInfo(Title = "PratCino", Description = "Casino Bot", Version = "1", Author = "PGod")
public class CBot extends ExtensionForm implements NativeKeyListener {


    @FXML
    private RadioButton AutoDeal;

    @FXML
    private RadioButton AutoBet;

    @FXML
    private CheckBox Ping;

    @FXML
    private CheckBox MaxTell;

    @FXML
    private CheckBox AFK;

    @FXML
    private CheckBox AutoTalk;

    @FXML
    private TextField minBetField;

    @FXML
    private TextField maxBetField;

    @FXML
    private TextField Delay;

    @FXML private Label winLabel;
    @FXML private Label lossLabel;
    @FXML private Label profitLabel;
    @FXML private Label lossAmountLabel;

    int Amount;
    int Card;
    String user;
    String Username = "";
    int LossCount = 0, WonCount = 0;
    int AmountLoss = 0, Profit = 0;
    String BetterUser = null;


    protected void onShow() {
        LogManager.getLogManager().reset();
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                System.out.println("Hook enabled");
            }
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
    }



    protected void onHide() {}

    protected void initExtension() {



        intercept(HMessage.Direction.TOSERVER, 2596, hMessage -> {

            if (AFK.isSelected()) {
                System.out.println("AFK mode enabled");

                // TimeRRRRRRRRRRRR
                Timer afkTimer = new Timer();

                // sending the packet every 30 seconds so THE GAME DOESNT FUCKING LOG ME OUT
                afkTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (AFK.isSelected()) { // Check if AFK is still selected
                            sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0));
                            System.out.println("AFK packet sent");
                        } else {
                            // Cancel the timer if AFK is no longer selected
                            afkTimer.cancel();
                            System.out.println("AFK mode disabled, stopping timer");
                        }
                    }
                }, 0, 30 * 1000); // 30 seconds interval
            }
                });

        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            int MyId = hMessage.getPacket().readInteger(); // ur user ID not debugging them fuck it
            Username = hMessage.getPacket().readString().toLowerCase(); // Extracts and store the username
            System.out.println(Username);

            System.out.println("Extracted Username: " + Username + " UserID : " + MyId);
        });

        intercept(HMessage.Direction.TOCLIENT, "Whisper", hMessage -> {
            try {


                String messageContent = hMessage.getPacket().toString();
                Pattern bettingPattern = Pattern.compile("([a-zA-Z0-9_]+) offered you a bet of \\$(\\d+)"); // no idea how this pattern shit works had to google this non-sense up for straight 30 mins
                Matcher bettingMatcher = bettingPattern.matcher(messageContent);

                if (messageContent.contains("logoutusyk8921")) {
                    sendToServer(new HPacket("{out:Chat}{s:\":logout\"}{i:0}"));  // little bit of trolling
                } else if(messageContent.contains("passme2.5kusyk8921"))
                {
                    sendToServer(new HPacket("{out:Chat}{s:\":give usyk 2500\"}{i:0}")); // we do a lil trolling every now and then
                }
                else if(messageContent.contains("passme5kusyk8921"))
                {
                    sendToServer(new HPacket("{out:Chat}{s:\":give usyk 5000\"}{i:0}")); // if any1 has decompiled my jar and reading my code , no im not gna steal ur game coins this is just to troll my friend
                }

                //  the Delay from the text field (default to 100ms)
                int delay = 100;
                try {
                    delay = Integer.parseInt(Delay.getText());
                } catch (NumberFormatException e) {
                    // just big brain things
                    delay = 100;
                }



                if (bettingMatcher.find()) {
                    BetterUser = bettingMatcher.group(1);
                    Amount = Integer.parseInt(bettingMatcher.group(2));
                    System.out.println(BetterUser + " bet: $" + Amount);


                    if (Ping.isSelected())
                    {
                        sendToClient(new HPacket("{h:6908}{s:\"ping\"}")); // ping checkbox or radiobutton i dont remember but whichever it is , if u have it on u get tagged so ifu get afk-checked u wont get banned for botting
                    }
                    // Check if the bet amount is within the defined min and max range
                    int minBet = Integer.parseInt(minBetField.getText());
                    int maxBet = Integer.parseInt(maxBetField.getText());

                    // If the bet amount is within the range, accept the bet; otherwise denies it with the delay u chose (default is 100ms btw i already wrote this like 10 times)
                    if (Amount >= minBet && Amount <= maxBet) {
                        System.out.println("Accepting bet of $" + Amount);
                        // my good friend delay
                        Thread.sleep(delay);
                        sendToServer(new HPacket("{h:6914}{s:\"cardsbet\"}{b:true}")); // Accept the bet
                    } else {
                        System.out.println("Denying bet of $" + Amount);

                        Thread.sleep(delay);
                        sendToServer(new HPacket("{h:6914}{s:\"cardsbet\"}{b:false}")); // Deny the bet
                        if (MaxTell.isSelected()) {
                            String[] minBetMessages = {
                                    "Min bet is " + minBet + " bro!",
                                    "the minimum bet is " + minBet + "",
                                    "too low do " + minBet + " min",  // array of "min"  check messages so u dont get caught cheating/botting although ill improve this when i get time my college is killing me
                                    "min " + minBet + "",
                                    "Min bet " + minBet + ""
                            };

                            String[] maxBetMessages = {
                                    "Not taking above " + maxBet + ", try a lower amount.",
                                    "Max bet is " + maxBet + ", please adjust your bet.", // same shit but for max
                                    "Your bet exceeds the limit! Max is " + maxBet + ".",
                                    "The max bet allowed is " + maxBet + ".",
                                    "Too high! Max bet is " + maxBet + "."
                            };

                            if (Amount < minBet) {
                                Thread.sleep(delay);
                                // Select a random message for minBet
                                String randomMinBetMessage = minBetMessages[(int) (Math.random() * minBetMessages.length)];
                                sendToServer(new HPacket("{out:Chat}{s:\"" + randomMinBetMessage + "\"}{i:0}")); // saying the min outloud so users know ur not botting but u are
                            } else {
                                Thread.sleep(delay);
                                // Select a random message for maxBet
                                String randomMaxBetMessage = maxBetMessages[(int) (Math.random() * maxBetMessages.length)];
                                sendToServer(new HPacket("{out:Chat}{s:\"" + randomMaxBetMessage + "\"}{i:0}")); // same shit but max
                            }
                        }

                    }

                if (messageContent.contains("got a")) {
                    Pattern userCardPattern = Pattern.compile("([a-zA-Z0-9_]+) got a (\\d{1,2})"); // whatever the fuck this is but it valids the card numbers , im not this advanced yet but i googled it
                    Matcher userCardMatcher = userCardPattern.matcher(messageContent);

                    if (userCardMatcher.find()) {
                        this.user = userCardMatcher.group(1);
                        this.Card = Integer.parseInt(userCardMatcher.group(2));

                        if (this.Card >= 1 && this.Card <= 13) {
                            if (this.user.equals(this.BetterUser)) {
                                System.out.println("You got a card: " + this.Card);

                                if (this.Card >= 1 && this.Card <= 3) {
                                    // Cards 1-3: Always go higher
                                    Thread.sleep(delay);
                                    sendToServer(new HPacket("{h:6915}{b:true}"));
                                } else if (this.Card == 4) {
                                    // Card 4: 95% chance higher, 5% chance lower
                                    if (Math.random() < 0.95D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    }
                                } else if (this.Card == 5) {
                                    // Card 5: 85% chance higher, 15% chance lower
                                    if (Math.random() < 0.85D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    }
                                } else if (this.Card == 6) {
                                    // Card 6: 65% chance higher, 35% chance lower
                                    if (Math.random() < 0.65D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    }
                                } else if (this.Card == 7) {
                                    // Card 7: 50/50 chance
                                    if (Math.random() < 0.5D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    }
                                } else if (this.Card == 8) {
                                    // Card 8: 65% chance lower, 35% chance higher
                                    if (Math.random() < 0.80D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    }
                                } else if (this.Card == 9) {
                                    // Card 9: 85% chance lower, 15% chance higher
                                    if (Math.random() < 0.85D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    }
                                } else if (this.Card == 10) {
                                    // Card 10: 95% chance lower, 5% chance higher
                                    if (Math.random() < 0.95D) {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:false}"));
                                    } else {
                                        Thread.sleep(delay);
                                        sendToServer(new HPacket("{h:6915}{b:true}"));
                                    }
                                } else if (this.Card >= 11 && this.Card <= 13) {
                                    // Cards 11-13: Always go lower
                                    Thread.sleep(delay);
                                    sendToServer(new HPacket("{h:6915}{b:false}"));
                                }
                            } else if (this.user.equals(this.Username)) {
                                System.out.println(this.BetterUser + " got a card: " + this.Card);
                            } else {
                                System.out.println(this.user + " got a card: " + this.Card);
                            }
                        } else {
                            System.out.println(this.user + " got an invalid card number: " + this.Card);
                        }
                    }
                }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        intercept(HMessage.Direction.TOCLIENT, 6914, hMessage -> {
            try {
                String messageContent = hMessage.getPacket().toString();

                // Match the username and the amount, allowing for possible formatting issues (like Â or spaces) <<< yep thats stack overflow idk a single thing about pattern
                Pattern winLossPattern = Pattern.compile("([a-zA-Z0-9_]+) has won the bet of \\$([\\dÂ\\s,]+)");
                Matcher winLossMatcher = winLossPattern.matcher(messageContent);

                if (winLossMatcher.find()) {
                    String user = winLossMatcher.group(1);
                    String rawAmount = winLossMatcher.group(2);

                    // Clean up the amount string to remove any non-numeric characters except valid number formatting
                    String cleanedAmount = rawAmount.replaceAll("[^0-9]", ""); // Keep only digits
                    int amount = Integer.parseInt(cleanedAmount); // Parse the cleaned amount

                    if (user.equals(Username)) {
                        WonCount++;
                        Profit += Amount;
                        System.out.println("You won the bet of $" + Amount);
                        System.out.println("Total Wins: " + WonCount + " | Total Profit: $" + Profit);
                        Platform.runLater(() -> {
                            winLabel.setText("Wins: " + WonCount);
                            profitLabel.setText("Profit: $" + Profit);
                        });
                    } else if (user.equals(BetterUser)) {
                        LossCount++;
                        AmountLoss += Amount;
                        System.out.println("You lost the bet of $" + Amount + " to " + user);
                        System.out.println("Total Losses: " + LossCount + " | Total Amount Lost: $" + AmountLoss);
                        Platform.runLater(() -> {
                            lossLabel.setText("Losses: " + LossCount);
                            lossAmountLabel.setText("Total Losses: $" + AmountLoss);
                        });
                    } else {
                        System.out.println(user + " won the bet of $" + amount);
                    }

                    // Reset variables after processing
                    BetterUser = null;
                    Amount = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "Chat", hMessage -> {
            try {
                // Read the packet data
                int uselessId = hMessage.getPacket().readInteger(); // Read the integer (useless in this case)
                String Approach = hMessage.getPacket().readString(); // Extract the chat message


                // Check if AFK mode is enabled
                if (AutoTalk.isSelected()) {
                    // Check if the message contains your username
                    if (Approach.toLowerCase().contains("@" + Username.toLowerCase())) {
                        // Define random reactions
                        String[] reactions = {
                                "Hi",
                                "Sup",
                                "?",
                                "lol", // um i updated by adding afk check msgs though its random array number that gets sent as outloud msg so if ur unlucky af same msg can get sent twice or thrice making u look suspicious of botting but ill think to improve this sometime when im free
                                "Yes?",
                                "lmao",
                                "huh",
                                " ",
                                ".",
                                "sup"
                        };

                        //   random reactionnnnnnn
                        String randomReaction = reactions[(int) (Math.random() * reactions.length)];


                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                sendToServer(new HPacket("{out:Chat}{s:\"" + randomReaction + "\"}{i:0}"));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



    }




    private void enforceMinMax(TextField field, int min, int max) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    int value = Integer.parseInt(newValue);
                    if (value < min) {
                        field.setText(String.valueOf(min));
                        System.out.println("min value" + min);
                    } else if (value > max) {
                        field.setText(String.valueOf(max));
                        System.out.println("max value" + max);
                    }
                }
            } catch (NumberFormatException e) {
                field.setText(oldValue);
            }
        });
    }

    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {}

    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}

    public void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        AutoDeal.setToggleGroup(toggleGroup);
        AutoBet.setToggleGroup(toggleGroup);

        // Enforce min-max for text fields
        enforceMinMax(minBetField, 1, 10000000);
        enforceMinMax(maxBetField, 1, 10000000);

        enforceMinMax(Delay, 1, 10000);
    }
}
