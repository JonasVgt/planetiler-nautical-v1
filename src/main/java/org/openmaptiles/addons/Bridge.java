package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.Translations;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;

public class Bridge implements Layer, OpenMapTilesProfile.OsmAllProcessor {

  private static final String LAYER_NAME = "seamark_bridge";

  Bridge(Translations translations, PlanetilerConfig config, Stats stats) {}

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {
    if (!feature.hasTag("seamark:type", "bridge"))
      return;

    String clazz = feature.getString("seamark:bridge:category");
    String clerance_height = feature.getString("seamark:bridge:clearance_height");
    if(clerance_height == null)
      clerance_height = feature.getString("seamark:bridge:clearance_height_closed");
    String clerance_width = feature.getString("seamark:bridge:clearance_width");

    features.anyGeometry("seamark_bridge")
      .setMinZoom(14)
      .setAttr("class", clazz)
      .setAttr("clearance_height", clerance_height)
      .setAttr("clearance_width", clerance_width);
  }

}
