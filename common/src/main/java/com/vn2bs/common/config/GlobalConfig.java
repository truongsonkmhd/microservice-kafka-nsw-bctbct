package com.vn2bs.common.config;

public interface GlobalConfig {
    public interface Kafka {
        public interface Topic {
            public interface NSW {
                public interface ThuTuc1 {
                    public final String GUI_HO_SO = "nsw-thutuc1-guihoso";
                }
            }
            public interface BCT {
                public interface ThuTuc1 {
                    public final String GUI_HO_SO = "bct-thutuc1-guihoso";
                    public final String TRA_LOI = "bct-thutuc1-traloi";
                    public final String TRA_LOI_WS = "bct-thutuc1-traloi-ws";
                }
            }
        }
    }
}
