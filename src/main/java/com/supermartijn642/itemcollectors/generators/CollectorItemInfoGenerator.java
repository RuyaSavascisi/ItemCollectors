package com.supermartijn642.itemcollectors.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.itemcollectors.ItemCollectors;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class CollectorItemInfoGenerator extends ItemInfoGenerator {

    public CollectorItemInfoGenerator(ResourceCache cache){
        super("itemcollectors", cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(ItemCollectors.basic_collector, "block/basic_collector");
        this.simpleInfo(ItemCollectors.advanced_collector, "block/advanced_collector");
    }
}
