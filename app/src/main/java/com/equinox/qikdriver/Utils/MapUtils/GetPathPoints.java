package com.equinox.qikdriver.Utils.MapUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Mukhtar on 8/26/2015.
 */
public class GetPathPoints {

    private static final String TOAST_MSG = "Getting Path Points";
    private static final String TOAST_ERR_MAJ = "Impossible to trace Itinerary";
    private Context context;
    private Hashtable<LatLng,LatLng> gMapPath = new Hashtable<>();

    public GetPathPoints(final Context context) {
        this.context = context;
    }

    public void execute(LatLng source, LatLng destination) {
        Toast.makeText(context, TOAST_MSG, Toast.LENGTH_LONG).show();
        gMapPath.clear();
        new AsyncTask<LatLng, Void, Integer>() {
            @Override
            protected Integer doInBackground(LatLng... params) {
                try {
                    final StringBuilder url = new StringBuilder("http://maps.googleapis.com/maps/api/directions/xml?language=eng&mode=walking");
                    url.append("&origin=");
                    url.append(params[0].latitude + '+' + params[0].longitude);
                    url.append("&destination=");
                    url.append(params[1].latitude + '+' + params[1].longitude);

                    final InputStream stream = new URL(url.toString()).openStream();
                    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    documentBuilderFactory.setIgnoringComments(true);
                    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    final Document document = documentBuilder.parse(stream);
                    document.getDocumentElement().normalize();

                    final String status = document.getElementsByTagName("status").item(0).getTextContent();
                    if (!"OK".equals(status)) {
                        return -1;
                    }
                    Log.d("FETCHED DIRECTIONS", "Hurraaay.. Fetched the directions for place: " + params[0] + ", " + params[1]);
                    final Element elementLeg = (Element) document.getElementsByTagName("leg").item(0);
                    final NodeList nodeListStep = elementLeg.getElementsByTagName("step");
                    final int length = nodeListStep.getLength();
                    for (int i = 0; i < length; i++) {
                        final Node nodeStep = nodeListStep.item(i);
                        if (nodeStep.getNodeType() == Node.ELEMENT_NODE) {
                            final Element elementStep = (Element) nodeStep;
                            decodePolylines(elementStep.getElementsByTagName("points").item(0).getTextContent());
                        }
                    }
                } catch (final Exception e) {
                    return -1;
                }
                return 1;
            }
            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                collector(result);
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, source, destination);

    }

    private void decodePolylines(final String encodedPoints) {
        int index = 0;
        int lat = 0, lng = 0;
        LatLng temp1, temp2 = null;
        while (index < encodedPoints.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encodedPoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift; shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat; shift = 0;
            result = 0;
            do {
                b = encodedPoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            temp1 = new LatLng((double)lat/1E5, (double)lng/1E5);
            gMapPath.put(temp1,temp2);
            temp2 = temp1;
        }
    }

    protected synchronized void collector(final Integer result) {
        if (result < 0) {
            Toast.makeText(context, TOAST_ERR_MAJ, Toast.LENGTH_SHORT).show();
            Log.d("Wrong direction", result.toString());
        }
        /*else if (count == places.size()){
            Toast.makeText(context, TOAST_MSG, Toast.LENGTH_LONG).show();
            RotaTask.setParams(context, gMap, editTo, editFrom, place, null, null, null, null);
            RotaTask.showPath();
        }*/
    }
}

