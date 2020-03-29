package com.network.message;

import javax.crypto.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.network.message.MessageNames.*;


public abstract class Message implements Serializable {

    private static final long serialVersionUID = 123;
    private final int code;
    private final String name;

    Message(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static class CryptedMessage extends Message {//19

        private byte[] cipheredBytes;

        private CryptedMessage(String name) {
            super(CryptedMessageCode, name);
        }

        public static CryptedMessage crypt(Message msg, String password) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
            CryptedMessage ciphered = new CryptedMessage(msg.getName());
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
            Key key = gen.generateKey();
            Cipher encCipher = Cipher.getInstance("AES");//TripleDES //посмотреть в ентернете
            encCipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream baos;
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new CipherOutputStream(
                            baos = new ByteArrayOutputStream(),
                            encCipher))) {
                oos.writeObject(msg);
            }
            ciphered.cipheredBytes = baos.toByteArray();////////////////////////////////////////////АаааааААААаааААААааFFFFFFFFFFawffwaafwfwwafff
            return ciphered;
        }

        public static Message decrypt(CryptedMessage ciphered, String password) throws IOException, ClassNotFoundException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
            Key key = gen.generateKey();
            Cipher decCipher = Cipher.getInstance("AES");//TripleDES //посмотреть в ентернете CBC ///ECB/PKCS5Padding
            decCipher.init(Cipher.DECRYPT_MODE, key);

            try (ObjectInputStream o = new ObjectInputStream(
                    new CipherInputStream(
                            new ByteArrayInputStream(ciphered.cipheredBytes),
                            decCipher))) {
                return (Message) o.readObject();
            }
        }
    }

    public static class GreetingMessage extends Message {//0

        private final int clientServerPartPort;
        private final String password;

        public GreetingMessage(String name, int clientServerPartPort, String password) {
            super(greetingMessageCode, name);
            this.clientServerPartPort = clientServerPartPort;
            this.password = password;
        }

        public int getClientServerPartPort() {
            return clientServerPartPort;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class EchoMessage extends Message {//1

        public EchoMessage() {
            super(echoMessageCode, "ECHO");
        }
    }

    public static class GreetingReplyMessage extends Message {//3

        private final boolean isAdmin;
        private final boolean isVerified;

        GreetingReplyMessage(boolean isAdmin, boolean isVerified) {
            super(greetingReplyMessageCode, "SERVER");
            this.isAdmin = isAdmin;
            this.isVerified = isVerified;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public boolean isVerified() {
            return isVerified;
        }
    }

    public static class RegistrationMessage extends Message {//4

        private final String password;

        public RegistrationMessage(String name, String password) {
            super(4, name);
            this.password = password;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class RegistrationReplyMessage extends Message {//5

        private final boolean isRegestrated;

        RegistrationReplyMessage(boolean isRegestrated) {
            super(registrationReplyMessageCode, "SERVER");
            this.isRegestrated = isRegestrated;
        }

        public boolean isRegistrated() {
            return isRegestrated;
        }
    }

    public static class SettingMessage extends Message {

        private final String newName;
        private final String password;
        private final InetSocketAddress inetSocketAddress;

        SettingMessage(String name, String newName, String password, InetSocketAddress inetSocketAddress) {
            super(settingMessageCode, name);
            this.newName = newName;
            this.password = password;
            this.inetSocketAddress = inetSocketAddress;
        }

        public String getNewName() {
            return newName;
        }

        public String getPassword() {
            return password;
        }

        public InetSocketAddress getInetSocketAddress() {
            return inetSocketAddress;
        }
    }

    public static class AddedClientMessage extends Message {//7

        AddedClientMessage(String name) {
            super(addedClientMessageCode, name);
        }
    }

    public static class DeleteClientMessage extends Message {//8

        DeleteClientMessage(String name) {
            super(deleteClientMessageCode, name);
        }
    }

    public static class DeleteMeMessage extends Message {//9

        private final InetSocketAddress inetSocketAddress;

        DeleteMeMessage(InetSocketAddress inetSocketAddress, String name) {
            super(deleteMeMessageCode, name);
            this.inetSocketAddress = inetSocketAddress;
        }

        public InetSocketAddress getInetSocketAddress() {
            return inetSocketAddress;
        }

    }

    public static class NewServerAddressMessage extends Message {//10

        private final InetSocketAddress inetSocketAddress;

        NewServerAddressMessage(InetSocketAddress inetSocketAddress) {
            super(newServerAddressMessageCode, "SERVER");
            this.inetSocketAddress = inetSocketAddress;
        }

        public InetSocketAddress getInetSocketAddress() {
            return inetSocketAddress;
        }
    }

    public static class SettingReplyMessage extends Message {//11

        private final boolean isChanged;

        SettingReplyMessage(boolean isChanged) {
            super(settingReplyMessageCode, "SERVER");
            this.isChanged = isChanged;
        }

        public boolean isChanged() {
            return isChanged;
        }
    }

    public static class AdminQueryMessage extends Message {//12

        private final Date date;
        private final String nameOfUser;
        private final String words;

        public AdminQueryMessage(String name, String nameOfUser, String words, Date date) {
            super(adminQueryMessageCode, name);
            this.date = date;
            this.nameOfUser = nameOfUser;
            this.words = words;
        }

        public Date getDate() {
            return date;
        }

        public String getNameOfUser() {
            return nameOfUser;
        }

        public String getWords() {
            return words;
        }
    }

    public static class AdminQueryReplyMessage extends Message {//13

        private List<Entry> list = new ArrayList<>();

        public AdminQueryReplyMessage() {
            super(adminQueryReplyMessageCode, "SERVER");
        }

        public List<Entry> getList() {
            return list;
        }

        static class Entry implements Serializable {

            private static final long serialVersionUID = 444;
            private final String date;
            private final String nameOfUser;
            private final String words;

            public Entry(String nameOfUser, String words, String date) {
                this.date = date;
                this.nameOfUser = nameOfUser;
                this.words = words;
            }

            public String getDate() {
                return date;
            }

            public String getNameOfUser() {
                return nameOfUser;
            }

            public String getWords() {
                return words;
            }
        }
    }

    public static class KastomQueryMessage extends Message {//14

        public KastomQueryMessage(String name) {
            super(kastomQueryMessageCode, name);
        }
    }

    public static class UserZakaziQueryMessage extends Message {//16

        public UserZakaziQueryMessage(String name) {
            super(userZakaziQueryMessageCode, name);
        }
    }

    public static class UserZakaziQueryReplyMessage extends Message {//17

        private Set<Zakaz> set = new HashSet<>();

        public UserZakaziQueryReplyMessage() {
            super(userZakaziQueryReplyMessageCode, "SERVER");
        }

        public Set<Zakaz> getSet() {
            return set;
        }

        static class Zakaz implements Serializable {

            public static final long serialVersionUID = 321;
            private final String date;
            private final String weapon;
            private final String scope;
            private final String podstvolnik;

            public Zakaz(String date, String weapon, String scope, String podstvolnik) {
                this.date = date;
                this.weapon = weapon;
                this.scope = scope;
                this.podstvolnik = podstvolnik;
            }

            public String getDate() {
                return date;
            }

            public String getWeapon() {
                return weapon;
            }

            public String getScope() {
                return scope;
            }

            public String getPodstvolnik() {
                return podstvolnik;
            }
        }
    }

    public static class NewZakazQueryMessage extends Message {//18

        private final String weapon;
        private final String scope;
        private final String podstvolnik;
        private final Date date = Date.valueOf(LocalDate.now());

        public NewZakazQueryMessage(String name, String weapon, String scope, String podstvolnik) {
            super(newZakazQueryMessageCode, name);
            this.weapon = weapon;
            this.scope = scope;
            this.podstvolnik = podstvolnik;
        }

        public String getWeapon() {
            return weapon;
        }

        public String getScope() {
            return scope;
        }

        public String getPodstvolnik() {
            return podstvolnik;
        }

        public Date getDate() {
            return date;
        }
    }

    public static class TextMessage extends Message {//2

        private final String text;

        TextMessage(String text, String name) {
            super(textMessageCode, name);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "TextMessage{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}