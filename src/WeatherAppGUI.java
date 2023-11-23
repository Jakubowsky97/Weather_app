import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGUI extends JFrame {
    private JSONObject weatherData;
    public WeatherAppGUI() {
        super("Weather App");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(450, 750);

         setLocationRelativeTo(null);

         setLayout(null);

         setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        // Search Bar
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15,15,351,45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        // Image of current weather
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // Temperature text
        JLabel temperatureText = new JLabel("10 °C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // Min Temperature text
        JLabel minTempText = new JLabel("Min");
        minTempText.setBounds(60, 375, 450, 54);
        minTempText.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(minTempText);

        // Min Temperature text
        JLabel minTempT = new JLabel("5 °C");
        minTempT.setBounds(55, 405, 450, 54);
        minTempT.setFont(new Font("Dialog", Font.BOLD, 24));
        add(minTempT);

        // Max Temperature text
        JLabel maxTempText = new JLabel("Max");
        maxTempText.setBounds(330, 375, 450, 54);
        maxTempText.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(maxTempText);

        // Max Temperature text
        JLabel maxTempT = new JLabel("15 °C");
        maxTempT.setBounds(325, 405, 450, 54);
        maxTempT.setFont(new Font("Dialog", Font.BOLD, 24));
        add(maxTempT);

        // Weather description text
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windSpeedImage.setBounds(220, 500, 74, 66);
        add(windSpeedImage);

        // wind speed text
        JLabel windSpeedText = new JLabel("<html><b>Wind speed</b> 15km/h</html>");
        windSpeedText.setBounds(310, 500, 90, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windSpeedText);

        // apparent temp text
        JLabel apparentTempText = new JLabel("<html>Apparent temp <b>4 °C</b></html>");
        apparentTempText.setBounds(175, 440, 110, 55);
        apparentTempText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(apparentTempText);

        // Submit Button with custom image
        JButton submitButton = new JButton(loadImage("src/assets/search.png"));
        submitButton.setBounds(375,13, 45,45);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText();

                if(userInput.replace("\\s", "").length() <= 0 ) {
                    return;
                }

                weatherData = WeatherApp.getWeatherData(userInput);

                if (weatherData == null) {
                    // Handle the case where weather data is not available
                    JOptionPane.showMessageDialog(null, "Weather data not available. Please check your input or try again later.");
                    return;
                }

                String weatherCondition = (String) weatherData.get("weather_condition");

                switch (weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                weatherConditionDesc.setText(weatherCondition);

                long humidity =  (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                double windspeed =  (double) weatherData.get("windspeed");
                windSpeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");

                double minTemp = (double) weatherData.get("temperature_min");
                minTempT.setText("<html><b>"+ minTemp + " °C</b></html>");

                double maxTemp = (double) weatherData.get("temperature_max");
                maxTempT.setText("<html><b>"+ maxTemp + " °C</b></html>");

                double apparentTemp = (double) weatherData.get("apparent_temp");
                apparentTempText.setText("<html>Apparent temp <b>" + apparentTemp + " °C</b></html>");

            }
        });
        add(submitButton);
    }

    private ImageIcon loadImage(String resourcePath) {
        try{
            BufferedImage image = ImageIO.read(new File(resourcePath));

            return new ImageIcon(image);
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource.");
        return null;
    }
}
