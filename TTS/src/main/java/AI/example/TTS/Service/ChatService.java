package AI.example.TTS.Service;
import org.springframework.stereotype.Service;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;

@Service
public class ChatService {

    private final OpenAiChatModel chatModel;

    public ChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String ask(String userQuestion, int targetWords) {
        String systemInstruction = "You are a helpful assistant. Respond clearly.";
        String userPrompt = "Question: " + userQuestion + "\n" +
                "Please answer in about " + targetWords + " words.";

        Prompt prompt = new Prompt(
                java.util.List.of(
                        new SystemMessage(systemInstruction),
                        new UserMessage(userPrompt)
                )
        );

        ChatResponse response = chatModel.call(prompt);
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text.trim();
    }
}
