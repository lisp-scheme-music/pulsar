package pulsar.lib.scheme.proc;

import gnu.mapping.Procedure3;

public abstract class PulsarProcedure3 extends Procedure3 implements MultipleNamed {
    public PulsarProcedure3() {
        super();
    }

    public PulsarProcedure3(String name) {
        super( name );
    }
}
