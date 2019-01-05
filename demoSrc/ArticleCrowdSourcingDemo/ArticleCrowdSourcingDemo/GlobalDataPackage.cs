using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ArticleCrowdSourcingDemo
{
    internal static class GlobalDataPackage
    {
        public const string DBServerIPAddress = "127.0.0.1";
        
        public const string DBUsername = "root";
        
        public const string DBPassword = "root";
        
        public const string DBName = "rencsdemo";

        public const string ProcessPID = "Process_156f7cf6-b66c-414e-b649-dbdac12dd91a";

        public const string Signature = "PrUpNw1dM3zRH6j3eviklCHE9Zbvk9NavGcJ_CibW19h50Yvr-ZZYZqn5Gi_SG1cPVQEIZf2wAJgBmq4dhNj7w7t9wUEz2pcGhn-6kIRO--QqWy121gksPE8B103RtMzuOsQDcErk4LriRQRO7-Xqks-RtpBUnpInnS_lkkajQs";

        // public const string URL_NS = "http://localhost";
        public const string URL_NS = "http://222.200.180.59";

        public const string URL_Callback = URL_NS + ":10234/ns/callback";

        public const string URL_GetAllWorkitem = URL_NS + ":10234/ns/workitem/getAll";

        public const string URL_WorkitemStart = URL_NS + ":10234/ns/workitem/start";

        public const string URL_WorkitemComplete = URL_NS + ":10234/ns/workitem/complete";

        public const string URL_Login = URL_NS + ":10234/auth/connect";

        public const string URL_SubmitProcess = URL_NS + ":10234/ns/submitProcess";

        public const string URL_UploadMapping = URL_NS + ":10234/rolemap/register";

        public const string URL_StartProcess = URL_NS + ":10234/ns/startProcess";

        public const string URL_LoadParticipant = URL_NS + ":10234/rolemap/loadParticipant";

        public static string RTID = "";

        public static string CurrentUsername = "";

        public static string CurrentUserWorkerId = "";

        public static string AuthToken = "";

        public static List<KeyValuePair<String, String>> Mappings { get; set; } = new List<KeyValuePair<string, string>>();

        public static UserViewRole CurrentUserViewRole = UserViewRole.Solver;
    }

    internal enum UserViewRole
    {
        Solver,
        Requester
    }

    internal enum SolvePhase
    {
        Solving,
        Solved
    }
}
