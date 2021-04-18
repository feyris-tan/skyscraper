using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;

namespace skyscraper2
{
    public class Ini : Dictionary<string,IniSection>
    {
        public Ini()
        {

        }

        [DebuggerStepThrough]
        public Ini (string filename)
        {
            string[] iniData = File.ReadAllLines (filename);
            IniSection currentSection = null;
            foreach (string line in iniData) {
                string s = line;
                s = s.Trim ();

                if (s.StartsWith (";"))
                    continue;

                if (s.Contains (";")) {
                    s = s.Split (';') [0];
                }

                if (s.StartsWith ("[") && s.EndsWith ("]")) {
                    string sectionName = s.Split ('[') [1].Split (']') [0];
                    currentSection = new IniSection ();
                    this [sectionName] = currentSection;
                    continue;
                }

                if (s.Contains ("=")) 
                {
                    string[] args = s.Split ('=');
                    args [0].Trim ();
                    args [1].Trim ();
                    currentSection [args [0]] = args [1];
                }
            }

        }

        public byte[] Export()
        {
            MemoryStream ms = new MemoryStream();
            StreamWriter sw = new StreamWriter(ms, Encoding.UTF8);
            foreach (KeyValuePair<string, IniSection> section in this)
            {
                sw.WriteLine("[{0}]", section.Key);
                foreach (KeyValuePair<string, string> entry in section.Value)
                {
                    sw.WriteLine("{0}={1}", entry.Key, entry.Value);
                }

                sw.WriteLine();
            }
            sw.Flush();
            return ms.ToArray();
        }
    }

    public class IniSection : Dictionary<string,string>
    {
    }
}