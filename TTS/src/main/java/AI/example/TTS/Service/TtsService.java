package AI.example.TTS.Service;

import org.springframework.stereotype.Service;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TtsService {

    private final OpenAiAudioSpeechModel speechModel;
    private static final int MAX_CHARS = 4000; // keep below 4096 limit

    public TtsService(OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    /**
     * Generate audio bytes (MP3/WAV depending on config) from very long text.
     * Splits text into smaller chunks to respect model limits.
     */
    public byte[] textToSpeechBytes(String text) {
        List<String> chunks = splitTextIntoChunks(text, MAX_CHARS);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String chunk : chunks) {
            SpeechPrompt speechPrompt = new SpeechPrompt(chunk);
            try {
                SpeechResponse response = this.speechModel.call(speechPrompt);
                byte[] chunkAudio = response.getResult().getOutput();
                outputStream.write(chunkAudio);
            } catch (Exception e) {
                throw new RuntimeException("TTS failed for a chunk", e);
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * Split text into smaller chunks under maxChars.
     * Tries to split at spaces to avoid breaking words.
     */
    private List<String> splitTextIntoChunks(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());

            // Optional: split at last space for cleaner sentences
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(" ", end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }
}
