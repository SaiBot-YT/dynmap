package org.dynmap.hdmap;

import java.util.BitSet;
import java.util.Map;

import org.dynmap.Log;
import org.dynmap.renderer.CustomRenderer;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.renderer.MapDataContext;
import org.dynmap.renderer.RenderPatch;

public class CustomBlockModel extends HDBlockModel {
    public CustomRenderer render;

    public CustomBlockModel(String blockname, BitSet databits, String classname, Map<String,String> classparm, String blockset) {
        super(blockname, databits, blockset);
        try {
            Class<?> cls = Class.forName(classname);   /* Get class */
            render = (CustomRenderer) cls.newInstance();
            if(render.initializeRenderer(HDBlockModels.pdf, blockname, databits, classparm) == false) {
                Log.severe("Error loading custom renderer - " + classname);
                render = null;
            }
            else {
                if(render.getTileEntityFieldsNeeded() != null) {
                    DynmapBlockState bbs = DynmapBlockState.getBaseStateByName(blockname);
                    for(int i = 0; i < bbs.getStateCount(); i++) {
                        if (databits.isEmpty() || databits.get(i)) {
                            DynmapBlockState bs = bbs.getState(i);
                            HDBlockModels.customModelsRequestingTileData.set(bs.globalStateIndex);
                        }
                    }
                }
            }
        } catch (Exception x) {
            Log.severe("Error loading custom renderer - " + classname, x);
            render = null;
        }
    }

    @Override
    public int getTextureCount() {
        return render.getMaximumTextureCount(HDBlockModels.pdf);
    }

    private static final RenderPatch[] empty_list = new RenderPatch[0];

    public RenderPatch[] getMeshForBlock(MapDataContext ctx) {
        if(render != null)
            return render.getRenderPatchList(ctx);
        else
            return empty_list;
    }
    @Override
    public void removed(DynmapBlockState blk) {
        super.removed(blk);
        HDBlockModels.customModelsRequestingTileData.clear(blk.globalStateIndex);
    }
}