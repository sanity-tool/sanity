package ru.urururu.sanity.cpp;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.Iterables;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBytecodeParser implements BytecodeParser {
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    ConstCache constants;

    @Override
    public List<Cfg> parse(File file) {
        try {
            URL url = new URL("http://10.0.75.2:32768/parse");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            os.write(Files.readAllBytes(file.toPath()));
            os.flush();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                System.out.println(response.toString());
            } else {
                throw new IllegalStateException("code was " + responseCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return Collections.emptyList();
    }
}
