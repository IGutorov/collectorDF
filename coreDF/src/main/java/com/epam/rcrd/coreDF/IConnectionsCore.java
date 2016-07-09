package com.epam.rcrd.coreDF;

import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

public interface IConnectionsCore {

    // 3. check both (IConnectionsCore)
    boolean checkBothConnections() throws Exception;

    // 5. get starter (IConnectionsCore)
    IMergerStarterExtension getNewMerger(TypeReconciliation type) throws Exception;

    // quick start
    // TypeReconciliation.getTypeByString
    // String aliasName1, String aliasName2, String strTypeRec  
    interface ICallBack {
        void threadCompleted();
    }

    interface IProgressIndicator {
        void StartProcess();

        void NextStep();

        void FinishProcess();
    }

    enum NumberConnection {
        FIRST_CONNECTION {
            @Override
            public String getSystemName() {
                return "System #1: ";
            }
        },
        SECOND_CONNECTION {
            @Override
            public String getSystemName() {
                return "System #2: ";
            }
        };

        public abstract String getSystemName();
    }
}
