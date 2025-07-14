package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.Translations;

import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;
import org.openmaptiles.util.OmtLanguageUtils;

public class Seamarks implements Layer, OpenMapTilesProfile.OsmAllProcessor {

  private static final String LAYER_NAME = "seamarks";
  
  private final Translations translations;

  Seamarks( Translations translations, PlanetilerConfig config, Stats stats) {
    this.translations = translations;
  }

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {
    if (!(feature.hasTag("seamark") || feature.tags().keySet().stream().anyMatch(k -> k.startsWith("seamark:"))))
      return;

    Object clazz = feature.getTag("seamark:type");
    Object subclass = clazz;
    
    if (feature.hasTag("seamark:" + clazz + ":category")) {
      subclass = feature.getTag("seamark:" + clazz + ":category");
    }


    if (feature.hasTag("seamark:type", "rock") && feature.hasTag("seamark:rock:water_level") ) {
      subclass = "rock_" + feature.getTag("seamark:rock:water_level");
    }

    if (feature.isPoint()) {
      features.point("seamarks")
          .setMinZoom(6)
          .putAttrs(OmtLanguageUtils.getNames(feature.tags(), translations))
          .setAttr("class", clazz)
          .setAttr("subclass", subclass);
    }

  }
}
