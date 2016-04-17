package com.epam.rcrd.coreDF;

import com.epam.common.igLib.ISaveTrace;
import com.epam.common.igLib.MapTraces.IUpdateStack;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

public interface IConnectionsCore {

    ISaveTrace registerReDrawer(IUpdateStack reDrawer);

    //    void setConnections(String aliasNameFirst, String aliasNameSecond) throws Exception;

    boolean checkBothConnections() throws Exception;

    IMergerStarter getNewMerger(TypeReconciliation type) throws Exception;

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
