using System;
using System.Collections.Generic;
using System.Data;

namespace RenMasterPanel
{
    internal static class GlobalContext
    {
        // public static readonly string URL_NS = "http://localhost";
        public static readonly string URL_NS = "http://222.200.180.59";

        public static readonly string URL_Auth_Connect = URL_NS + ":10234/auth/connect";
        
        public static readonly string URL_GetProcessByRenId = URL_NS + ":10234/ns/getProcessByRenId";

        public static readonly string URL_GetProcessBOByPid = URL_NS + ":10234/ns/getProcessBOList";

        public static readonly string URL_CreateProcess = URL_NS + ":10234/ns/createProcess";

        public static readonly string URL_UploadBO = URL_NS + ":10234/ns/uploadBO";

        public static readonly string URL_SubmitProcess = URL_NS + ":10234/ns/submitProcess";

        public static readonly string URL_LaunchProcess = URL_NS + ":10234/ns/launchProcess";

        public static readonly string URL_GetDataVersion = URL_NS + ":10234/rolemap/getDataVersionAndGidFromCOrgan";

        public static readonly string URL_GetAllResources = URL_NS + ":10234/rolemap/getAllResourceFromCOrgan";

        public static readonly string URL_GetAllRelationConnections = URL_NS + ":10234/rolemap/getAllConnectionFromCOrgan";
        
        public static readonly string URL_UploadMapping = URL_NS + ":10234/rolemap/register";
        
        public static readonly string URL_LoadParticipant = URL_NS + ":10234/rolemap/loadParticipant";

        public static List<Dictionary<String, String>> Current_Ren_Process_List = null;

        public static String ResourcesCOrganGid = null;

        public static String ResourcesDataVersion = null;

        public static DataSet ResourcesDataSet = null;

        public static String CurrentRTID = null;

        public static String CurrentProcessSelfSignature = null;
    }
}
