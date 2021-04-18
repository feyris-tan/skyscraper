using System;
using System.Collections.Generic;
using System.Text;
using skyscraper2.TsDuckInterface.DvbModel;

namespace skyscraper2.TsDuckInterface.Inputs
{
    class DvbInput : TspInput
    {
        public byte? AdapterId { get; set; }
        public byte? SatelliteNumber { get; set; }
        public Lnb? Lnb { get; set; }
        public DeliverySystem DeliverySystem { get; set; }
        public long? Frequency { get; set; }
        public long? SymbolRate { get; set; }
        public Polarity? Polarity { get; set; }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("dvb ");
            if (AdapterId.HasValue)
                sb.AppendFormat("-a {0} ", AdapterId);

            if (Frequency != null)
                sb.AppendFormat("--frequency {0} ", Frequency);

            if (DeliverySystem != null)
                sb.AppendFormat("--delivery-system {0} ", DeliverySystem.ToString());

            if (Lnb.HasValue)
                sb.AppendFormat("--lnb {0} ", Lnb.ToString());

            if (Polarity.HasValue)
                sb.AppendFormat("--polarity {0} ", Polarity.ToString().ToLowerInvariant());

            if (SatelliteNumber.HasValue)
                sb.AppendFormat("--satellite-number {0} ", SatelliteNumber);

            if (SymbolRate.HasValue)
                sb.AppendFormat("--symbol-rate {0} ", SymbolRate);

            return sb.ToString().Trim();
        }
    }
}
