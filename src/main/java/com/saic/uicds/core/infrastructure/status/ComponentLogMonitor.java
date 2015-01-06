package com.saic.uicds.core.infrastructure.status;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.saic.uicds.core.infrastructure.log.LogFileMonitor;
import com.saic.uicds.core.infrastructure.log.LogFileMonitor.LogFileMonitorRunnable;
import com.usersmarts.util.DirectoryWatcher;
import com.usersmarts.util.DirectoryWatcher.Change;

public class ComponentLogMonitor implements StatusEventMonitor, InitializingBean,
        DirectoryWatcher.Listener, DisposableBean {

    private List<File> logFiles = new ArrayList<File>();

    private int pollingDelay;

    private String logName;

    private String logPattern;

    Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Level> fileLevelMap = new HashMap<String, Level>();

    private String UICDS_TIMESTAMP = "HH:mm:ss";

    private String OPENDS_TIMESTAMP = "dd/MMM/yyyy:HH:mm:ss Z";

    private Map<String, LogFileMonitor> logMonitors = new HashMap<String, LogFileMonitor>();;

    public int getPollingDelay() {
        return pollingDelay;
    }

    public void setPollingDelay(int pollingDelay) {
        this.pollingDelay = pollingDelay;
    }

    private Level thresholdLevel;

    private Date thresholdTimeStamp = new Date();

    public Date getThresholdTimeStamp() {
        return thresholdTimeStamp;
    }

    public void setThresholdTimeStamp(Date thresholdTimeStamp) {
        this.thresholdTimeStamp = thresholdTimeStamp;
    }

    private boolean doTailing = false;

    private List<StatusEventListener> listeners = new ArrayList<StatusEventListener>();

    private DirectoryWatcher logDirWatcher;
    private Timer watcherTimer;
    private long frequency = 20000;
    private long delay = 5000;
    private Date rollOverDate = new Date();
    private String rollOverDateFormat;
    private boolean isRolloverFile = false;
    private String rolloverType = null;
    private List<Timer> timers = new ArrayList<Timer>();
    private List<TimerTask> tasks = new ArrayList<TimerTask>();
    private List<LogFileMonitorRunnable> runnables = new ArrayList<LogFileMonitorRunnable>();

    public String getRollOverDateFormat() {
        return rollOverDateFormat;
    }

    public void setRollOverDateFormat(String rollOverDateFormat) {
        this.rollOverDateFormat = rollOverDateFormat;
    }

    private List<String> fileKeyWords = new ArrayList<String>();

    public ComponentLogMonitor() {
    }

    @Override
    public void doPost(LoggingEvent event) {
        // Receive loggign event from log file
        Level eventLevel = event.getLevel();
        if (logName.equals("opends") || logName.equals("openfire")) {
            String file = event.getProperty("fileUrl").replace("file:///", "");
            eventLevel = fileLevelMap.get(file);
        }
        Level threshold = getThresholdLevel();
        if (eventLevel.isGreaterOrEqual(threshold)) {
            processEvent(event); // process event if level greater than or equals threshold
        }
    }

    private void setTimer(int timeOut) {
        Timer timer = new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                pollLogFilesForChanges();
            }
        };
        timer.schedule(task, timeOut);
        timers.add(timer);
        tasks.add(task);
    }

    public String findIssues(StatusEvent event) {
        return null;
    }

    public List<File> getLogFiles() {
        return logFiles;
    }

    public String getLogName() {
        return logName;
    }

    public Level getThresholdLevel() {
        return thresholdLevel;
    }

    public void initializePolling() {
        pollLogFilesForChanges(); // start polling log files
    }

    private Date getEventDate(LoggingEvent event) {
        // Parse timestamp from loggingEvent
        Date date = new Date(event.getTimeStamp());
        String relTime = event.getProperty("RELATIVETIME"); // timestamp stored as Relativetime
        SimpleDateFormat dateFormat = new SimpleDateFormat(event.getProperty("timeStamp"));
        if (logName.equals("uicds")) {
            dateFormat = new SimpleDateFormat(UICDS_TIMESTAMP);
        } else if (logName.equals("opends")) {
            dateFormat = new SimpleDateFormat(OPENDS_TIMESTAMP);
        }
        try {
            if (relTime != null) {
                if (Character.isDigit(relTime.charAt(0))) {
                    if (logName.equals("openfire")) {
                        if (relTime.length() < 12) {
                            String time = (String) event.getMessage();
                            relTime = relTime + " " + time.substring(0, 9).trim();
                        }
                    }
                    date = dateFormat.parse(relTime);
                } else
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (logName.equals("uicds")) {
            Date currentDate = new Date();
            date.setYear(currentDate.getYear());
            date.setMonth(currentDate.getMonth());
            date.setDate(currentDate.getDate());
        }
        return date;
    }

    public boolean isDoTailing() {
        return doTailing;
    }

    public void pollLogFilesForChanges() {
        Date thresoldTimeStamp = new Date();
        setThresholdTimeStamp(thresoldTimeStamp);
        List<File> thresholdFiles = getThresholdLogFiles(); // get multiple component files that are
                                                            // above threshold (openfire,..)
        if (thresholdFiles.size() == 0) {
            thresholdFiles = logFiles; // singecomponent files
        }
        for (final File logFile : thresholdFiles) {
            if (logFile.exists()) {
                fileKeyWords.add(logFile.getName());
                createLogMonitor(logFile);
            }
        }
        if (isRolloverFile)
            startDirectoryWatcher(thresholdFiles);
    }

    private void createLogMonitor(File logFile) {
        try {
            Timer timer = new Timer();
            if (logName.equals("openfire")) {
                if (!logFile.getName().contains("error")) {
                    logPattern = "PROP(RELATIVETIME) MESSAGE";
                    Thread.sleep(2000);
                    // TODO Auto-generated catch block
                } else
                    logPattern = "PROP(RELATIVETIME) [CLASS(FILE:LINE)";
            }
            final LogFileMonitor logmonitor = new LogFileMonitor(logName, logPattern, doTailing,
                    thresholdLevel, logFile);
            logmonitor.addMonitors(this);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    LogFileMonitorRunnable lfrun=logmonitor.new LogFileMonitorRunnable(logmonitor);
                    runnables.add(lfrun);
                    lfrun.run();
                }

            };
            timer.schedule(task, 5000); // start polling after 3ms
            timers.add(timer);
            tasks.add(task);
            logMonitors.put(logFile.getAbsolutePath(), logmonitor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDirectoryWatcher(List<File> thresholdFiles) {
        if (!thresholdFiles.isEmpty()) {
            File logDirectory = thresholdFiles.get(0).getParentFile();
            logDirWatcher = new DirectoryWatcher(logDirectory, this, null, true);
            watcherTimer = new Timer();
            watcherTimer.schedule(logDirWatcher, delay, frequency);
            timers.add(watcherTimer);
            tasks.add(logDirWatcher);
        }
    }

    private List<File> getThresholdLogFiles() {
        List<File> results = new ArrayList<File>();
        for (File file : logFiles) {
            String levelName = extractLevelName(file.getName());
            Level level;
            if (levelName != null) {
                level = Level.toLevel(levelName);
                fileLevelMap.put(file.getAbsolutePath(), level);
                if (level.isGreaterOrEqual(thresholdLevel)) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    public void shutdown() {
        for (LogFileMonitor monitor : logMonitors.values()) {
            monitor.setTailing(false);
        }
        for(LogFileMonitorRunnable runnable:runnables){
            runnable.stop();
        }
    }

    private String extractLevelName(String name) {
        String levelName = name.replace(".log", "");
        if (levelName.contains("error"))
            return "ERROR";
        else if (levelName.contains("warn"))
            return "WARN";
        else if (levelName.contains("debug"))
            return "DEBUG";
        else if (levelName.contains("info"))
            return "INFO";
        return null;
    }

    private void processEvent(LoggingEvent event) {
        String componentId = logName;
        String level = event.getLevel().toString();
        if (logName.equals("opends") || logName.equals("openfire")) {
            String file = event.getProperty("fileUrl").replace("file:///", "");
            level = fileLevelMap.get(file).toString();
        }
        String message = (String) event.getMessage();
        String[] throwInfo = event.getThrowableInformation().getThrowableStrRep(); // use throw
                                                                                   // information if
                                                                                   // logging event
                                                                                   // has it as
                                                                                   // message
        if (throwInfo != null) {
            if (throwInfo[0] != "") {
                message = "";
                for (String info : throwInfo) {
                    message += info + "\n";
                }
            }
        }
        Date date = getEventDate(event);
        StatusEvent statusEvent = new StatusEvent(componentId, level, message, date);
        notifyOnEvent(statusEvent);
    }

    public void setLogFiles(List<File> logFiles) {
        this.logFiles = new ArrayList<File>();
        for (File logFile : logFiles) {
            String fileName = logFile.getAbsolutePath();
            String format = extractFormat(logFile.getName());
            if (format != null) {
                isRolloverFile = true;
                if (format.equals("N")) {
                    rolloverType = "size";
                    fileName = fileName.replace(".{N}", "");
                } else {
                    rolloverType = "day";
                    setRollOverDateFormat(format);
                    DateFormat df = new SimpleDateFormat(format);
                    String ds = df.format(new Date());
                    fileName = fileName.replace(format, ds).replace("{", "").replace("}", "");
                }
            }
            this.logFiles.add(new File(fileName));
        }
    }

    private String extractFormat(String logFileName) {
        String format = null;
        if (logFileName.contains("{")) {
            int start = logFileName.indexOf("{") + 1;
            int end = logFileName.indexOf("}");
            format = logFileName.substring(start, end);
        }
        return format;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public void setDoTailing(boolean tailing) {
        this.doTailing = tailing;
    }

    public void setThresholdLevel(Level thresholdLevel) {
        this.thresholdLevel = thresholdLevel;
    }

    @Override
    public void notifyOnEvent(StatusEvent event) {
        for (StatusEventListener listener : listeners) {
            listener.handleStatusEvent(event);
        }
    }

    @Override
    public void addListener(StatusEventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(StatusEventListener listener) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (pollingDelay > 0)
            setTimer(pollingDelay); // start polling after pollingDelay milliseconds
    }

    @Override
    public void onChange(File file, Change change) {
        String fileName = file.getName();
        if (!fileName.contains(".log"))
            return;
        boolean contians = false;
        for (String keyword : fileKeyWords) {
            if (keyword.contains(fileName.substring(0, fileName.indexOf(".")))) {
                contians = true;
                break;
            }
        }
        if (!contians)
            return;
        try {
            if (Change.ADDED.equals(change)) {
                if (rolloverType.equals("size")) {
                    sizeRollover(file);
                } else {
                    dayRollover(file);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void dayRollover(File file) throws Exception {
        String fileName = file.getName();
        String fileDateString = extractFileDate(fileName);
        if (fileDateString != null) {
            DateFormat df = new SimpleDateFormat(rollOverDateFormat);
            Date fileDate = df.parse(fileDateString);
            if (fileDate.after(rollOverDate)) {
                LogFileMonitor monitor = null;
                for (String monitorKey : logMonitors.keySet()) {
                    String dateString = df.format(rollOverDate);
                    if (monitorKey.contains(dateString)) {
                        monitor = logMonitors.get(monitorKey);
                        rolloverLogMonitor(file, monitor);
                        logMonitors.remove(monitorKey);
                        createLogMonitor(file);
                        rollOverDate = new Date();
                        break;
                    }
                }
            }
        }
    }

    private void sizeRollover(File file) {
        String fileName = file.getName();
        String fileKey = fileName.substring(0, fileName.indexOf("."));
        LogFileMonitor monitor = null;
        for (String monitorKey : logMonitors.keySet()) {
            if (monitorKey.contains(fileKey)) {
                monitor = logMonitors.get(monitorKey);
                rolloverLogMonitor(file, monitor);
                logMonitors.remove(monitorKey);
                createLogMonitor(new File(monitorKey));
                break;
            }
        }
    }

    private void rolloverLogMonitor(File file, LogFileMonitor monitor) {
        monitor.setTailing(false);
        monitor.shutdown();
    }

    private String extractFileDate(String fileName) throws Exception {
        if (fileName.contains("-")) {
            String dateString = fileName.substring(fileName.indexOf(".") + 1);
            dateString = dateString.replace("log.", "");
            dateString = dateString.replace(".log", "");
            return dateString;
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
        if (!tasks.isEmpty()) {
            for (TimerTask task : tasks) {
                task.cancel();
            }
            tasks.clear();
        }
        if (!timers.isEmpty()) {
            for (Timer timer : timers) {
                timer.cancel();
            }
            timers.clear();
        }
    }

}
