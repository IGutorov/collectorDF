package com.epam.rcrd.coreDF;

import com.epam.common.igLib.Money100;
import com.epam.common.igLib.LegendArrayOfNamedObjects;
import com.epam.common.igLib.LegendArrayOfNamedObjects.ClassField;

final class PackageConsts {

    // scheduler params
    static final String PARAM_TURN = "Turn";   // ��� ������� �� ���� (end of date ������)
    static final String PARAM_REST = "Rest";   // ��� ������� �� ���� (end of date ������)
    static final String PARAM_ACCS = "AccStat"; // ������� �� ����� �� ������ (end of date ������)
    static final String PARAM_DOCS = "Docs";   // ��������� ��  ���� (online ������)

    private PackageConsts() {
    }

    interface IGetResultSet {
        IParamPutGet[] getResultArray();
    }

    interface IParamPutGet {
        Object getObject(String keyData);

        void put(String keyData, Object value);
    }

    static final byte TURN_CHARTYPE_NOT_DEFINED = 0;
    static final byte TURN_CHARTYPE_DEBET       = 1;
    static final byte TURN_CHARTYPE_CREDIT      = 2;

    static byte negativeCharType(final byte in) {
        switch (in) {
            case TURN_CHARTYPE_DEBET:
                return TURN_CHARTYPE_CREDIT;
            case TURN_CHARTYPE_CREDIT:
                return TURN_CHARTYPE_DEBET;
            default:
                return TURN_CHARTYPE_NOT_DEFINED;
        }
    }

    /*    
        // �����������:
        static final int EQUALS                = 0;   // ��� �����������
        // "������" ��� "�����"
        static final int NOT_EXISTS_MASTER     = 101; // ��� � Master �������
        static final int DUBLICATE_DOC_GL      = 102; // � ���� GL ��� ��������� � ���������� �������
        static final int NOT_EXISTS_GL         = 201; // ��� � ������� �����
        static final int DUBLICATE_DOC_MASTER  = 202; // � ��������� ���� ��� ��������� � ���������� �������
        // ���� � � GL � � Master, �� �� �����
        static final int NOT_EQUAL_DOCNUMBER   = 301; // �� ��������� ����� ��������� (���� ���������), ��� �/� ����
        static final int NOT_EQUAL_AMOUNT      = 302; // ����� �� �����
        static final int NOT_EQUAL_ACCOUNT_DEB = 303; // �� ��������� ���� �� ������
        static final int NOT_EQUAL_ACCOUNT_CRE = 304; // �� ��������� ���� �� �������
        static final int NOT_EQUAL_AMOUNT_DEB  = 305; // �� ��������� ����� �� ������
        static final int NOT_EQUAL_AMOUNT_CRE  = 306; // �� ��������� ����� �� �������

        static boolean onlyGeneral(final int diffType) {
            return (diffType == NOT_EXISTS_MASTER || diffType == DUBLICATE_DOC_GL);
        }

        static boolean onlyMaster(final int diffType) {
            return (diffType == NOT_EXISTS_GL || diffType == DUBLICATE_DOC_MASTER);
        }
    */
    enum DifferenceType {
        EQUALS {               // ��� �����������
            @Override
            boolean isError() {
                return false;
            };
        },
        NOT_EXISTS_MASTER {    // ��� � Master �������
            @Override
            boolean onlyGeneral() {
                return true;
            }
        },
        DUBLICATE_DOC_GL {     // � ���� General ��� ��������� � ���������� �������
            @Override
            boolean onlyGeneral() {
                return true;
            }
        },
        NOT_EXISTS_GL {        // ��� � General
            @Override
            boolean onlyMaster() {
                return true;
            }
        },
        DUBLICATE_DOC_MASTER { // � Master ������� ��� ��������� � ���������� �������
            @Override
            boolean onlyMaster() {
                return true;
            }
        },
        // ���� � � General � � Master, �� �� �����
        NOT_EQUAL_DOCNUMBER,   // �� ��������� ����� ��������� (���� ���������), ��� �/� ����
        NOT_EQUAL_AMOUNT,      // ����� �� �����
        NOT_EQUAL_ACCOUNT_DEB, // �� ��������� ���� �� ������
        NOT_EQUAL_ACCOUNT_CRE, // �� ��������� ���� �� �������
        NOT_EQUAL_AMOUNT_DEB,  // �� ��������� ����� �� ������
        NOT_EQUAL_AMOUNT_CRE;  // �� ��������� ����� �� �������

        boolean onlyGeneral() {
            return false;
        };

        boolean onlyMaster() {
            return false;
        };

        boolean isError() {
            return true;
        };
}

    enum ConnectionSide {
        LEFT, RIGHT
    }

    enum FetchSide {
        LEFT {
            @Override
            final boolean isLeft() {
                return true;
            }

            @Override
            final boolean isRight() {
                return false;
            }

            @Override
            final boolean isBoth() {
                return false;
            }
        },
        RIGHT {
            @Override
            final boolean isLeft() {
                return false;
            }

            @Override
            final boolean isRight() {
                return true;
            }

            @Override
            final boolean isBoth() {
                return false;
            }
        },
        BOTH {
            @Override
            final boolean isLeft() {
                return true;
            }

            @Override
            final boolean isRight() {
                return true;
            }

            @Override
            final boolean isBoth() {
                return true;
            }
        };

        static FetchSide getFetchSide(final int compare) {
            switch (Integer.signum(compare)) {
                case -1:
                    return LEFT;
                case 1:
                    return RIGHT;
                default:
                    return BOTH;
            }
        }

        static FetchSide getFetchSide(final boolean leftFetched, final boolean rightFetched) {
            if (!leftFetched)
                return RIGHT;
            if (!rightFetched)
                return LEFT;
            return BOTH;
        }

        abstract boolean isLeft();

        abstract boolean isRight();

        abstract boolean isBoth();
    }

    static final ClassField[]              TURN_FIELDS    = new ClassField[] {
            new ClassField("rowNumber     ", Integer.class), new ClassField("diffType         ", String.class),
            new ClassField("resourceNumber ", String.class), new ClassField("turnCharType     ", String.class),
            new ClassField("externalID     ", String.class), new ClassField("turnAmount     ", Money100.class),
            new ClassField("docNumber      ", String.class), new ClassField("docID            ", String.class),
            new ClassField("deltaAmount  ", Money100.class), new ClassField("detailMesssage   ", String.class),
            new ClassField("batchBrief     ", String.class), new ClassField("debResNumber     ", String.class),
            new ClassField("creResNumber   ", String.class) };

    static final ClassField[]              REST_FIELDS    = new ClassField[] {
            new ClassField("rowNumber     ", Integer.class), new ClassField("diffType         ", String.class),
            new ClassField("resourceNumber ", String.class), new ClassField("restGL         ", Money100.class),
            new ClassField("restMaster   ", Money100.class), new ClassField("deltaRest      ", Money100.class) };

    static final ClassField[]              DOCS_FIELDS    = new ClassField[] {
            new ClassField("rowNumber     ", Integer.class), new ClassField("diffType         ", String.class),
            new ClassField("resourceNumber ", String.class), new ClassField("externalID       ", String.class),
            new ClassField("docAmount    ", Money100.class), new ClassField("docNumber        ", String.class),
            new ClassField("docID          ", String.class), new ClassField("deltaAmountDoc ", Money100.class),
            new ClassField("detailMesssage ", String.class) };

    static final ClassField[]              SUMM_FIELDS    = new ClassField[] {
            new ClassField("mainCriterion  ", String.class), new ClassField("secondCriterion  ", String.class),
            new ClassField("leftCount     ", Integer.class), new ClassField("leftAmount     ", Money100.class),
            new ClassField("rightCount    ", Integer.class), new ClassField("rightAmount    ", Money100.class),
            new ClassField("notEqualsCount", Integer.class), new ClassField("deltaAmount    ", Money100.class) };

    static final LegendArrayOfNamedObjects LEGEND_TURN    = new LegendArrayOfNamedObjects(TURN_FIELDS);
    static final LegendArrayOfNamedObjects LEGEND_REST    = new LegendArrayOfNamedObjects(REST_FIELDS);
    static final LegendArrayOfNamedObjects LEGEND_DOCS    = new LegendArrayOfNamedObjects(DOCS_FIELDS);

    static final LegendArrayOfNamedObjects LEGEND_SUMMARY = new LegendArrayOfNamedObjects(SUMM_FIELDS);
}
