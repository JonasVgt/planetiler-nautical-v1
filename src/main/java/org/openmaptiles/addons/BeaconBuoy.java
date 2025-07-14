package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.Parse;
import com.onthegomap.planetiler.util.Translations;
import java.util.Set;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;
import org.openmaptiles.util.OmtLanguageUtils;

public class BeaconBuoy implements Layer, OpenMapTilesProfile.OsmAllProcessor {

  private static final String LAYER_NAME = "beacon_buoy";

  private final Translations translations;

  private final Set<String> SUPPORTED_CLASSES = Set.of(
    "beacon_cardinal",
    "beacon_isolated_danger",
    "beacon_lateral",
    "beacon_safe_water",
    "beacon_special_purpose",
    "buoy_cardinal",
    "buoy_installation",
    "buoy_isolated_danger",
    "buoy_lateral",
    "buoy_safe_water",
    "buoy_special_purpose");

  BeaconBuoy(Translations translations, PlanetilerConfig config, Stats stats) {
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

    String clazz = Parse.parseStringOrNull(feature.getTag("seamark:type"));

    if (clazz == null || !SUPPORTED_CLASSES.contains(clazz)) {
      return;
    }

    Object subclass = clazz;

    if (feature.hasTag("seamark:" + clazz + ":category")) {
      subclass = feature.getTag("seamark:" + clazz + ":category");
    }

    if (feature.isPoint()) {
      features.point("beacon_buoy")
        .setMinZoom(6)
        .putAttrs(OmtLanguageUtils.getNames(feature.tags(), translations))
        .setAttr("class", clazz)
        .setAttr("subclass", subclass)
        .setAttr("shape", feature.getTag("seamark:" + clazz + ":shape"))
        .setAttr("colour", parse_colour(feature.getTag("seamark:" + clazz + ":colour")))
        .setAttr("colour_pattern", feature.getTag("seamark:" + clazz + ":colour_pattern"))
        .setAttr("topmark_shape", feature.getTag("seamark:topmark:shape"))
        .setAttr("topmark_colour", parse_colour(feature.getTag("seamark:topmark:colour")));
      }

  }

  private Object parse_colour(Object tag){
    String str = Parse.parseStringOrNull(tag);
    if(str == null) return null;
    return str.replace(";", "_");
  }
}
