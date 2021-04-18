using System;
using System.Collections.Generic;
using System.Text;

namespace skyscraper2.TsDuckInterface
{
    class DeliverySystem
    {
        public string Name { get; }

        private DeliverySystem(string name)
        {
            Name = name;
        }

        public override string ToString()
        {
            return Name;
        }

        public static readonly DeliverySystem ATSC = new DeliverySystem("ATSC");
        public static readonly DeliverySystem ATSC_MH = new DeliverySystem("ATSC-MH");
        public static readonly DeliverySystem CMMB = new DeliverySystem("CMMB");
        public static readonly DeliverySystem DAB = new DeliverySystem("DAB");
        public static readonly DeliverySystem DSS = new DeliverySystem("DSS");
        public static readonly DeliverySystem DTMB = new DeliverySystem("DTMB");
        public static readonly DeliverySystem DVB_C = new DeliverySystem("DVB-C");
        public static readonly DeliverySystem DVB_C_AnnexA = new DeliverySystem("DVB-C/A");
        public static readonly DeliverySystem DVB_C_AnnexB = new DeliverySystem("DVB-C/B");
        public static readonly DeliverySystem DVB_C_AnnexC = new DeliverySystem("DVB-C/C");
        public static readonly DeliverySystem DVB_C2 = new DeliverySystem("DVB-C2");
        public static readonly DeliverySystem DVB_H = new DeliverySystem("DVB-H");
        public static readonly DeliverySystem DVB_S = new DeliverySystem("DVB-S");
        public static readonly DeliverySystem DVB_S_Turbo = new DeliverySystem("DVB-S-Turbo");
        public static readonly DeliverySystem DVB_S2 = new DeliverySystem("DVB-S2");
        public static readonly DeliverySystem DVB_T = new DeliverySystem("DVB-T");
        public static readonly DeliverySystem DVB_T2 = new DeliverySystem("DVB-T2");
        public static readonly DeliverySystem ISDB_C = new DeliverySystem("ISDB-C");
        public static readonly DeliverySystem ISDB_S = new DeliverySystem("ISDB-S");
        public static readonly DeliverySystem ISDB_T = new DeliverySystem("ISDB-T");
    }
}
