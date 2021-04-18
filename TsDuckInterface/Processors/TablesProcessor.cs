using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace skyscraper2.TsDuckInterface.Processors
{
    class TablesProcessor : TspProcessor
    {
        public bool AllOnce { get; set; }
        public bool StictXml { get; set; }
        public FileInfo XmlOutput { get; set; }
        public bool PackAllSections { get; set; }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            if (AllOnce)
                sb.Append("--all-once ");
            if (StictXml)
                sb.Append("--strict-xml ");
            if (XmlOutput != null)
                sb.AppendFormat("--xml-output \"{0}\" ", XmlOutput.FullName);
            if (PackAllSections)
                sb.Append("--pack-all-sections ");

            return sb.ToString().Trim();
        }
    }
}
