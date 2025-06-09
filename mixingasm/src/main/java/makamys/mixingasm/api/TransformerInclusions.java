package makamys.mixingasm.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.Launch;

public class TransformerInclusions {
    
    private static final String INCLUSION_LIST_BLACKBOARD_KEY = "mixingasm.transformerInclusionList";
    
    /** Returns Mixingasm's dynamic transformer inclusion list. Add transformer name patterns to this list if you want to spare them from being added to
     * the mixin environment's transformer exclusion list, for example if you need to mix into a version of a class that has been transformed by them.
     * <br><br>
     * <b>Note:</b> this needs to be called before the DEFAULT phase.
     */
    public static List<String> getTransformerInclusionList(){
        List<String> list = (List<String>)Launch.blackboard.get(INCLUSION_LIST_BLACKBOARD_KEY);
        if(list == null) {
            Launch.blackboard.put(INCLUSION_LIST_BLACKBOARD_KEY, list = new ArrayList<String>());
        }
        return list;
    }
    
}
