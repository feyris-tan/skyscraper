using System;
using System.Collections.Generic;
using System.Text;

namespace skyscraper2.TsDuckInterface.Processors
{
    class UntilProcessor : TspProcessor
    {
        public int Seconds { get; }

        public UntilProcessor(int seconds)
        {
            Seconds = seconds;
        }

        public override string ToString()
        {
            return String.Format("until --seconds {0}", Seconds);
        }
    }
}
