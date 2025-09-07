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

    String subclass = clazz;

    if (feature.hasTag("seamark:" + clazz + ":category")) {
      subclass = feature.getString("seamark:" + clazz + ":category");
    }

    if (feature.isPoint()) {
      features.point("beacon_buoy")
          .setMinZoom(6)
          .putAttrs(OmtLanguageUtils.getNames(feature.tags(), translations))
          .setAttr("class", clazz)
          .setAttr("subclass", subclass)
          .setAttr("buoy", clazz.startsWith("buoy"))
          .setAttr("shape", get_shape(feature, clazz, subclass))
          .setAttr("pattern", get_pattern(feature, clazz, subclass))
          .setAttr("topmark_shape", get_topmark_shape(feature, clazz, subclass))
          .setAttr("topmark_pattern", get_topmark_pattern(feature, clazz, subclass));
    }

  }

  private String parse_pattern(String colour, String pattern) {
    if (colour == null)
      return null;

    if (pattern == null)
      return colour.replace(";", "_");

    return pattern + "/" + colour.replace(";", "_");
  }

  private String get_shape(SourceFeature feature, String clazz, String subclass) {
    String shape = feature.getString("seamark:" + clazz + ":shape");
    if (shape != null)
      return shape;

    switch (clazz) {
      case "buoy_cardinal":
      case "buoy_isolated_danger":
      case "buoy_lateral":
        return "pillar";
      case "buoy_safe_water":
        return "buoy_spherical";
      case "buoy_special_purpose":
        return "barrel";
      case "beacon_cardinal":
      case "beacon_isolated_danger":
      case "beacon_safe_water":
      case "beacon_special_purpose":
        return "pile";
      case "beacon_lateral":
        switch (subclass) {
          case "port":
          case "starboard":
          case "waterway_right":
          case "waterway_left":
          case "channel_right_bank":
          case "channel_left_bank":
          case "crossover_right":
          case "crossover_left":
          case "danger_right":
          case "danger_left":
          case "waterway_separation":
          case "channel_separation":
            return "stake";
          case "channel_right":
          case "channel_left":
          case "harbour_right":
          case "harbour_left":
          case "preferred_channel_port":
          case "preferred_channel_starboard":
          default:
            return "pile";
        }
      default:
        return null;
    }
  }

  private String get_pattern(SourceFeature feature, String clazz, String subclass) {

    String pattern = parse_pattern(feature.getString("seamark:" + clazz + ":colour"),
        feature.getString("seamark:" + clazz + ":colour_pattern"));
    if (pattern != null)
      return pattern;

    switch (clazz) {
      case "beacon_cardinal":
      case "buoy_cardinal":
        switch (subclass) {
          case "north":
            return "horizontal/black_yellow";
          case "east":
            return "horizontal/black_yellow_black";
          case "south":
            return "horizontal/yellow_black";
          case "west":
            return "horizontal/yellow_black_yellow";
          default:
            return null;
        }
      case "beacon_isolated_danger":
      case "buoy_isolated_danger":
        return "horizontal/black_red_black";
      case "beacon_lateral":
      case "buoy_lateral":
        if (get_shape(feature, clazz, subclass) == "stake") {
          return "black";
        }
        if (feature.getString("seamark:beacon_lateral:system") == "iala-b") {
          switch (subclass) {
            case "port":
            case "waterway_right":
            case "channel_right":
              return "green";
            case "starboard":
            case "waterway_left":
            case "channel_left":
              return "red";
            case "harbour_left":
            case "danger_left":
              return "horizontal/red_white_red_white";
            case "harbour_right":
            case "danger_right":
              return "horizontal/green_white_green_white";
            case "preferred_channel_port":
            case "turnoff_left":
              return "horizontal/red_green_red";
            case "preferred_channel_starboard":
            case "turnoff_right":
              return "horizontal/green_red_green";
            case "waterway_seperation":
            case "channel_separation":
              return "horizontal/red_green_red_green";
            default:
              return "generic";
          }
        } else {
          switch (subclass) {
            case "port":
            case "waterway_right":
            case "channel_right":
              return "red";
            case "starboard":
            case "waterway_left":
            case "channel_left":
              return "green";
            case "harbour_left":
            case "danger_left":
              return "horizontal/green_white_green_white";
            case "harbour_right":
            case "danger_right":
              return "horizontal/red_white_red_white";
            case "preferred_channel_port":
            case "turnoff_left":
              return "horizontal/green_red_green";
            case "preferred_channel_starboard":
            case "turnoff_right":
              return "horizontal/red_green_red";
            case "waterway_seperation":
            case "channel_separation":
              return "horizontal/red_green_red_green";
            default:
              return "generic";
          }
        }
      case "buoy_safe_water":
      case "beacon_safe_water":
        return "vertical/red_white";
      case "beacon_special_purpose":
      case "buoy_special_purpose":
        return "yellow";
      default:
        return "generic";
    }

  }

  private String get_topmark_shape(SourceFeature feature, String clazz, String subclass) {
    String topmark_shape = feature.getString("seamark:topmark:shape");
    if (topmark_shape == null)
      return null;

    topmark_shape = topmark_shape.replace(',', ' ');

    topmark_shape = topmark_shape.replaceAll("\s+", "_");

    return topmark_shape;
  }

  private String get_topmark_pattern(SourceFeature feature, String clazz, String subclass) {
    String topmark_pattern = parse_pattern(feature.getString("seamark:topmark:colour"),
        feature.getString("seamark:topmark:colour_pattern"));
    if (topmark_pattern != null)
      return topmark_pattern;

    if (get_topmark_shape(feature, clazz, subclass) == null) {
      return null;
    }

    switch (clazz) {
      case "beacon_cardinal":
      case "beacon_isolated_danger":
      case "buoy_cardinal":
      case "buoy_isolated_danger":
        return "black";
      case "beacon_lateral":
      case "buoy_lateral":
        if (feature.getString("seamark:beacon_lateral:system") == "iala-b") {
          switch (subclass) {
            case "port":
            case "waterway_right":
            case "channel_right":
              return "green";
            case "starboard":
            case "waterway_left":
            case "channel_left":
              return "red";
            case "harbour_left":
            case "danger_left":
              return "horizontal/red_white_red_white";
            case "harbour_right":
            case "danger_right":
              return "horizontal/green_white_green_white";
            case "preferred_channel_port":
            case "turnoff_left":
              return "horizontal/red_green_red";
            case "preferred_channel_starboard":
            case "turnoff_right":
              return "horizontal/green_red_green";
            case "waterway_seperation":
            case "channel_separation":
              return "horizontal/red_green_red_green";
            default:
              return "generic";
          }
        } else {
          switch (subclass) {
            case "port":
            case "waterway_right":
            case "danger_right":
            case "channel_right":
            case "harbour_right":
            case "turnoff_right":
              return "red";
            case "starboard":
            case "waterway_left":
            case "danger_left":
            case "channel_left":
            case "harbour_left":
            case "preferred_channel_port":
            case "turnoff_left":
              return "green";
            case "channel_right_bank":
              return "border/white_red";
            case "channel_left_bank":
              return "border/white_green";
            case "crossover_right":
            case "crossover_left":
              return "yellow";
            case "preferred_channel_starboard":
              if (get_topmark_shape(feature, clazz, subclass) == "2_cones_point_together")
                return "horizontal/red_green";
              else
                return "red";
            case "channel_separation":
            case "waterway_seperation":
              return "horizontal/red_green";

            default:
              return "generic";
          }
        }
      case "beacon_safe_water":
      case "buoy_safe_water":
        return "red";
      case "beacon_special_purpose":
      case "buoy_special_purpose":
        return "yellow";
      default:
        return "generic";
    }
  }
}