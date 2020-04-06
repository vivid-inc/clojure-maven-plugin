package vivid.cmp.messages;

import io.vavr.control.Option;
import vivid.cmp.mojo.AbstractCMPMojo;

/**
 * User-facing message.
 */
public interface Message {

    Option<Exception> getCause();

    String render(
            final AbstractCMPMojo mojo
    );

}
