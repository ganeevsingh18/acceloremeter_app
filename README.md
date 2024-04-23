

---

# Android Accelerometer App and Data Analysis

## Overview
This application utilizes the accelerometer sensor on Android devices to track and display the real-time orientation of the smartphone in terms of three angles (X, Y, and Z). The app collects orientation data over time, stores it in a database, and visualizes this historical data through three separate graphs. Additionally, the app includes functionality to export this data, allowing for further analysis, such as time series prediction of future values.

## Features
- **Real-Time Orientation Tracking**: Displays current orientation of the device dynamically as the device moves.
- **Data Collection**: Stores orientation data over time in a local database.
- **Graphical History Display**: Shows past orientation data through graphical plots for X, Y, and Z axes.
- **Data Export**: Exports the stored orientation data to a CSV file for external use.
- **Time Series Prediction**: A separate Python script uses historical data to predict future orientation values.

## Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/your-repository.git
   ```
2. **Open the project** in Android Studio.
3. **Run the application** on an Android device or emulator.

## Usage
### Android App
- **Main Activity**: Displays real-time orientation data and provides a button to navigate to the historical data graphs.
- **History Activity**: Shows graphs for X, Y, and Z orientations over time.
- **Export Data**: Use the "Export Data" button to save the orientation data to a CSV file.

### Data Analysis
- **Ensure Python is installed** on your machine. You can download Python from [python.org](https://www.python.org/downloads/).
- **Install necessary Python packages**:
  ```bash
  pip install pandas matplotlib numpy statsmodels
  ```
- **Run the script**:
  ```bash
  python time_series_prediction.py
  ```

## Files
- **Android App**:
  - `MainActivity.kt`: Manages real-time data display and navigation.
  - `HistoryActivity.kt`: Handles the display of historical data graphs.
  - `OrientationDataModel.kt`: Data model for orientation data.
  - `AppDatabase.kt`: Room database setup for storing orientation data.
- **Python Script**:
  - `time_series_prediction.py`: Script for loading the exported CSV file and performing time series prediction.

## Data Export and Analysis
After exporting the data to a CSV file from the Android app, transfer this file to your machine to conduct further analysis with the provided Python script. The script predicts the next 10 seconds of orientation values based on historical data and plots these predictions against actual values.

## Changing Sensing Intervals
To experiment with different sensing intervals:
1. Modify the sensing interval in the Android app via the settings menu.
2. Export the data after running the app for each interval.
3. Use the Python script to analyze how the interval change affects data predictions.

## Contributing
Contributions to this project are welcome. Please fork the repository and submit a pull request with your enhancements.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
