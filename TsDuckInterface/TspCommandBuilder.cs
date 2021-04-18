using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;

namespace skyscraper2.TsDuckInterface
{
    class TspCommandBuilder
    {
        public TspCommandBuilder()
        {
            Processors = new List<TspProcessor>();
            TspPath = "tsp";
        }
        
        public string TspPath { get; set; }

        public TspInput Input { get; set; }
        public TspOutput Output { get; set; }
        public List<TspProcessor> Processors { get; }
        public bool Verbose { get; set; }

        public void ExecuteAndWait()
        {
            if (Input == null)
                throw new NullReferenceException("No Input specified!");
            if (Output == null)
                throw new NullReferenceException("No output specified!");

            StringBuilder sb = new StringBuilder();
            if (Verbose)
                sb.Append("-v ");

            sb.Append("-I ");
            sb.Append(Input.ToString());
            sb.Append(" ");

            foreach (TspProcessor processor in Processors)
            {
                sb.Append("-P ");
                sb.Append(processor.ToString());
                sb.Append(" ");
            }

            sb.Append("-O ");
            sb.Append(Output.ToString());
            sb.Append(" ");

            Process tsp = new Process();
            tsp.StartInfo.FileName = TspPath;
            tsp.StartInfo.Arguments = sb.ToString();

            tsp.Start();
            tsp.WaitForExit();
        }
    }
}
