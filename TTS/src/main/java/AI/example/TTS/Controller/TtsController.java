package AI.example.TTS.Controller;



import AI.example.TTS.Service.ChatService;
import AI.example.TTS.Service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final ChatService chatService;
    private final TtsService ttsService;



    // Request model for JSON body
    public static record TtsRequest(String question, Integer wordCount) {}

    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateSpeech(@RequestBody TtsRequest req) {
        if (req == null || req.question() == null || req.question().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Word count validation (1–1000)
        int requested = (req.wordCount() == null) ? 150 : req.wordCount();
        requested = Math.max(1, Math.min(requested, 1000));

        // 1️⃣ Generate answer text
        String rawAnswer = chatService.ask(req.question(), requested);

        // 2️⃣ Trim text to requested words
        String trimmed = trimToWords(rawAnswer, requested);

        // 3️⃣ Convert to speech
        byte[] audioBytes = ttsService.textToSpeechBytes(trimmed);

        // 4️⃣ Set headers for download (unique filename each time)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));

        // Unique filename using timestamp
        String filename = "response_" + System.currentTimeMillis() + ".mp3";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }

    // Helper method to ensure the text fits within word count
    private String trimToWords(String text, int maxWords) {
        if (text == null || text.isBlank()) return "";
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) return text.trim();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) sb.append(' ');
            sb.append(words[i]);
        }
        sb.append("...");
        return sb.toString();
    }
}