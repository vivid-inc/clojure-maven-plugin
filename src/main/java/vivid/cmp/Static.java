package vivid.cmp;

import vivid.cherimoya.annotation.Constant;

public class Static {

    private static final String CONSTANT_REASON =
            "Users prefer their existing Maven POM vivid:clojure-maven-plugin configuration to " +
                    "remain compatible as-is with newer versions of this Maven plugin.";

    @Constant(rationale = CONSTANT_REASON)
    static final String POM_CMP_ARGS_PROPERTY_KEY = "args";

    @Constant(rationale = CONSTANT_REASON)
    static final String POM_CMP_CLOJURE_MOJO_NAME = "clojure";

}
