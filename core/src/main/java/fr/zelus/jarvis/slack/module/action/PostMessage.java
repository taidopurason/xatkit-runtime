package fr.zelus.jarvis.slack.module.action;

import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import fr.inria.atlanmod.commons.log.Log;
import fr.zelus.jarvis.core.JarvisAction;
import fr.zelus.jarvis.core.JarvisException;
import fr.zelus.jarvis.slack.module.SlackModule;

import java.io.IOException;
import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * A {@link JarvisAction} that posts a {@code message} to a given Slack {@code channel}.
 * <p>
 * This class relies on the {@link SlackModule}'s {@link com.github.seratch.jslack.Slack} client and Slack bot API token
 * to connect to the Slack API and post a message.
 * <p>
 * <b>Note:</b> this class requires that its containing {@link SlackModule} has been loaded with a valid Slack bot API
 * token in order to authenticate the bot and post messages.
 */
public class PostMessage extends JarvisAction {

    /**
     * The message to post.
     */
    private String message;

    /**
     * The Slack channel to post the message in.
     */
    private String channel;

    /**
     * Constructs a new {@link PostMessage} with the provided {@code message} and {@code channel}.
     *
     * @param message the message to post
     * @param channel the Slack channel to post the message to
     * @throws IllegalArgumentException if the provided {@code message} or {@code channel} is {@code null} or empty.
     */
    public PostMessage(String message, String channel) {
        super();
        checkArgument(nonNull(message) && !message.isEmpty(), "Cannot construct a {0} action with the provided " +
                "message {1}, expected a non-null and not empty String", this.getClass().getSimpleName(), message);
        checkArgument(nonNull(channel) && !channel.isEmpty(), "Cannot construct a {0} action with the provided " +
                "channel {1}, expected a non-null and not empty String", this.getClass().getSimpleName(), channel);
        this.message = message;
        this.channel = channel;
    }

    /**
     * Posts the provided {@code message} to the given {@code channel}.
     * <p>
     * This method relies on the containing {@link SlackModule}'s Slack bot API token to authenticate the bot and
     * post the {@code message} to the given {@code channel}.
     *
     * @throws IOException       if an error occurs when connecting to the Slack API
     * @throws SlackApiException if the provided token does not authenticate the bot
     */
    @Override
    public void run() {
        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .token(SlackModule.slackToken)
                .channel(channel)
                .text(message)
                .build();
        try {
            ChatPostMessageResponse response = SlackModule.slack.methods().chatPostMessage(request);
            if (response.isOk()) {
                Log.trace("Message {0} successfully sent to the Slack API", request);
            } else {
                Log.error("Cannot send the message {0} to the Slack API, received response {1}", request, response);
            }
        } catch (IOException | SlackApiException e) {
            String errorMessage = MessageFormat.format("Cannot send the message {0} to the Slack API", request);
            Log.error(errorMessage);
            throw new JarvisException(errorMessage, e);
        }
    }
}