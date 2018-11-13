#-help                                  - Show this help.
#-version                               - Print the version information and quit.
#-session=<ADTFSESSIONFILE>             - Specify the ADTF Session.
#-adtf-core-plugin=<ADTFCOREPLUGINFILE> - Specify a dedicated adtf-core-plugin to load. (default is adtf_core.adtfplugin).
#-system=<ADTFSYSTEMFILE>               - Specify the ADTF system configuration.
#-system-properties=<PROPERTIESFILE>    - Specify the ADTF system properties file.
#-graph=<ADTFGRAPHFILE>                 - Specify the ADTF graph file. (requires -system)
#-graph-properties=<PROPERTIESFILE>     - Specify the ADTF graph properties file. (requires -system)
#-active-streaming-graph=<GRAPHNAME>    - Specify the active streaminggraph. (requires -system)
#-active-filter-graph=<GRAPHNAME>       - Specify the active filtergraph. (requires -system)
#-control-url=<SCHEMA:HOST:PORT>        - URL to connect to this system (default '" ADTF_REMOTE_DEFAULT_HOST_URL "').
#-init                                  - Initialize current configuration.
#-run                                   - Start the current configuration after.
#-quit                                  - Shutdown application after reaching the end of file/playlist,the end of a script or after stopping the configuration.
#-quit=<SECONDS>                        - Shutdown the application after the specified amount of time.
#-profiler                              - Enables the profiler.
#-profiler-port=<PORT>                  - Sets the port for the remote connection to the profiler. 0 = no remote profiling, default = 28077.
#-profiler-dump-file=<FILENAME>         - Enables profiling right from the start. When ADTF shuts down, profiling data will be dumped to the given file.
#-log-file=<LOGFILE>                    - Writes all stdout output to the given logfile.
#                                         The stderr output will be written to \"<LOGFILE>_stderr.txt\".
#                                         To get both outputs in one file use: \"adtf_launcher > logfile.txt 2>&1\"
#-log-processing-interval=<INTERVAL=0[microseconds]>
#                                       - If not equal zero, log messages will be processed asynchronously at the given interval.
#-log-level-stdout=<LOGLEVEL=all>       - The log level limit(none, error, warning, info, dump, all) for writing log entries to stdout.
#-log-level-stderr=<LOGLEVEL=none>      - The log level limit(none, error, warning, info, dump, all) for writing log entries to stderr.
#-log-level-debug=<LOGLEVEL=none>       - The log level limit(none, error, warning, info, dump, all) for writing log entries to windows debug output.
#-console                               - Redirects stdout and stderr to a (new if needed) console window.
#                                         This does not affect whether log messages are written to stdout or not.
#                                         If you pipe the output, do not use this flag. Suppresses a dump file creation dialog.

# first try to kill old python before starting ADTF
pkill -f -SIGUSR1 object_detection_server.py

cd /opt/ADTF/3.3.3/bin
./adtf_launcher -session=/home/aadc/AADC/src/aadcUser/taco/server/config/2018/TACO_ADTF/adtfsessions/Playback.adtfsession -run

