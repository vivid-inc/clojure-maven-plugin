package vivid.cmp.messages;

import io.vavr.control.Option;
import vivid.cmp.mojo.AbstractCMPMojo;

/**
 * User-facing message.
 */
public interface Message {

    default Option<Exception> getCause() {
        return Option.none();
    }

    String render(
            final AbstractCMPMojo mojo
    );

}
