package com.relationshipradar.app.shizuku;

interface IShellService {
    void destroy() = 16777114; // reserved by Shizuku
    void exit() = 1;
    /** Runs one command from the fixed allow-list. Returns "ok:<output>" or "err:<output>". */
    String run(int commandId) = 2;
    /** Reads one query from the fixed allow-list (dumpsys etc.). */
    String query(int queryId) = 3;
}
