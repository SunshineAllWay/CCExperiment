package org.apache.solr.search.function.distance;
import org.apache.solr.common.SolrException;
public class DistanceUtils {
  public static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
  public static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;
  public static final double KM_TO_MILES = 0.621371192;
  public static final double MILES_TO_KM = 1.609344;
  public static double vectorDistance(double[] vec1, double[] vec2, double power) {
    return vectorDistance(vec1, vec2, power, 1.0 / power);
  }
  public static double vectorDistance(double[] vec1, double[] vec2, double power, double oneOverPower) {
    double result = 0;
    if (power == 0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i] == 0 ? 0 : 1;
      }
    } else if (power == 1.0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i];
      }
    } else if (power == 2.0) {
      result = Math.sqrt(squaredEuclideanDistance(vec1, vec2));
    } else if (power == Integer.MAX_VALUE || Double.isInfinite(power)) {
      for (int i = 0; i < vec1.length; i++) {
        result = Math.max(result, Math.max(vec1[i], vec2[i]));
      }
    } else {
      for (int i = 0; i < vec1.length; i++) {
        result += Math.pow(vec1[i] - vec2[i], power);
      }
      result = Math.pow(result, oneOverPower);
    }
    return result;
  }
  public static double squaredEuclideanDistance(double[] vec1, double[] vec2) {
    double result = 0;
    for (int i = 0; i < vec1.length; i++) {
      double v = vec1[i] - vec2[i];
      result += v * v;
    }
    return result;
  }
  public static double haversine(double x1, double y1, double x2, double y2, double radius) {
    double result = 0;
    if ((x1 != x2) || (y1 != y2)) {
      double diffX = x1 - x2;
      double diffY = y1 - y2;
      double hsinX = Math.sin(diffX * 0.5);
      double hsinY = Math.sin(diffY * 0.5);
      double h = hsinX * hsinX +
              (Math.cos(x1) * Math.cos(x2) * hsinY * hsinY);
      result = (radius * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h)));
    }
    return result;
  }
  public static String[] parsePoint(String[] out, String externalVal, int dimension) {
    if (out == null || out.length != dimension) out = new String[dimension];
    int idx = externalVal.indexOf(',');
    int end = idx;
    int start = 0;
    int i = 0;
    if (idx == -1 && dimension == 1 && externalVal.length() > 0) {
      out[0] = externalVal.trim();
      i = 1;
    } else if (idx > 0) {
      for (; i < dimension; i++) {
        while (start < end && externalVal.charAt(start) == ' ') start++;
        while (end > start && externalVal.charAt(end - 1) == ' ') end--;
	if (start == end){
	    break;
        }
        out[i] = externalVal.substring(start, end);
        start = idx + 1;
        end = externalVal.indexOf(',', start);
	idx = end;
        if (end == -1) {
          end = externalVal.length();
        }
      }
    }
    if (i != dimension) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "incompatible dimension (" + dimension +
              ") and values (" + externalVal + ").  Only " + i + " values specified");
    }
    return out;
  }
  public static double[] parsePointDouble(double[] out, String externalVal, int dimension) {
    if (out == null || out.length != dimension) out = new double[dimension];
    int idx = externalVal.indexOf(',');
    int end = idx;
    int start = 0;
    int i = 0;
    if (idx == -1 && dimension == 1 && externalVal.length() > 0) {
      out[0] = Double.parseDouble(externalVal.trim());
      i = 1;
    } else if (idx > 0) {
      for (; i < dimension; i++) {
        while (start < end && externalVal.charAt(start) == ' ') start++;
        while (end > start && externalVal.charAt(end - 1) == ' ') end--;
	if (start == end){
	    break;
        }
        out[i] = Double.parseDouble(externalVal.substring(start, end));
        start = idx + 1;
        end = externalVal.indexOf(',', start);
	idx = end;
        if (end == -1) {
          end = externalVal.length();
        }
      }
    }
    if (i != dimension) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "incompatible dimension (" + dimension +
              ") and values (" + externalVal + ").  Only " + i + " values specified");
    }
    return out;
  }
  public static final double[] parseLatitudeLongitude(double[] latLon, String latLonStr) {
    if (latLon == null) {
      latLon = new double[2];
    }
    double[] toks = DistanceUtils.parsePointDouble(null, latLonStr, 2);
    latLon[0] = Double.valueOf(toks[0]);
    if (latLon[0] < -90.0 || latLon[0] > 90.0) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "Invalid latitude: latitudes are range -90 to 90: provided lat: ["
                      + latLon[0] + "]");
    }
    latLon[1] = Double.valueOf(toks[1]);
    if (latLon[1] < -180.0 || latLon[1] > 180.0) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "Invalid longitude: longitudes are range -180 to 180: provided lon: ["
                      + latLon[1] + "]");
    }
    return latLon;
  }
}
