package ru.urururu.sanity.cpp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.ConstCache;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
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
            urlConnection.addRequestProperty("Content-Type", "application/octet-stream");
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            os.write(Files.readAllBytes(file.toPath()));
            os.flush();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                ObjectMapper mapper = new ObjectMapper();
                JsonNode module = mapper.readTree(urlConnection.getInputStream());

                return parseTree(module);
            } else {
                throw new IllegalStateException("code was " + responseCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Cfg> parseTree(JsonNode module) {
        return null;
    }
}
