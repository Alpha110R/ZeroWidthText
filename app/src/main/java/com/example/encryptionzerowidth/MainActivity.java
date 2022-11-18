package com.example.encryptionzerowidth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
//Alon Ronder 208272278
public class MainActivity extends AppCompatActivity {
    private TextInputEditText main_EDT_textToHide;
    private MaterialButton main_BTN_copyToClipBoard, main_BTN_hideText;
    private String encodeMessage="";
    private String decryptedMessage="";
    private TextView main_TXT_decryptedText;
    private TextView main_TXT_encodeText;
    private ClipData clip;
    private ClipboardManager clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        main_BTN_hideText.setOnClickListener(view ->{
            String messageToEncode = main_EDT_textToHide.getText().toString();
            encodeMessage = encrypt(messageToEncode, "Inside this text there is your text");
            decryptedMessage  = decrypt(encodeMessage);
            main_TXT_decryptedText.setText("Original Text: " + decryptedMessage);
            main_TXT_encodeText.setText("Text After Hiding: " + encodeMessage);
        });

        main_BTN_copyToClipBoard.setOnClickListener(view ->{
            clip = ClipData.newPlainText("Alon Ronder App", encodeMessage);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text Copied To Clip Board", Toast.LENGTH_LONG).show();
        });
    }

    private void findViews() {
        main_EDT_textToHide = findViewById(R.id.main_EDT_textToHide);
        main_BTN_copyToClipBoard = findViewById(R.id.main_BTN_copyToClipBoard);
        main_BTN_hideText = findViewById(R.id.main_BTN_hideText);
        main_TXT_decryptedText = (TextView) findViewById(R.id.main_TXT_decryptedText);
        main_TXT_encodeText = (TextView) findViewById(R.id.main_TXT_encodeText);
    }

    public static String encrypt(String hiddenText, String visibleText) {
        byte[] visibleTextLength = ByteBuffer.allocate(4).putInt(visibleText.length()).array();
        System.out.println("visibleTextLength A: " + visibleText.length());

        StringBuilder secret = new StringBuilder();

        secret.append(visibleText.charAt(0));
        // insert visibleText length in text
        for (byte b : visibleTextLength) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                secret.append((val & 128) == 0 ? '\u200B' : '\u200D');
                val <<= 1;
            }
        }


        byte[] bytes = hiddenText.getBytes();

        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                secret.append((val & 128) == 0 ? '\u200B' : '\u200D');
                val <<= 1;
            }
        }

        secret.append(visibleText.substring(1));
        System.out.println("secretA: " + secret.toString());
        System.out.println("secretB: " + new String(bytes, StandardCharsets.UTF_8));

        return secret.toString();
    }

    public static String decrypt(String encryptedText) {
        encryptedText = encryptedText.substring(1);

        char[] chars = encryptedText.toCharArray();

        // Get length og original displayed string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append(chars[i*8+j] == '\u200B' ? 0 : 1);
            }
        }

        int visibleTextLength = Integer.parseInt(sb.toString(), 2);
        System.out.println("visibleTextLength B: " + visibleTextLength);
        sb.setLength(0);

        encryptedText = encryptedText.substring(0, encryptedText.length() - visibleTextLength + 1);
        chars = encryptedText.toCharArray();
        byte[] bytes = new byte[chars.length / 8];

        // Start after 4 bytes of int value (the length)
        for (int i = 4; i < bytes.length; i++) {
            for (int j = i*8; j < (i*8)+8; j++) {
                sb.append(chars[j] == '\u200B' ? 0 : 1);
            }
            bytes[i] = (byte) Integer.parseInt(sb.toString(), 2);
            sb.setLength(0);
        }

        String real = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("real: " + real);

        return real;
    }
}