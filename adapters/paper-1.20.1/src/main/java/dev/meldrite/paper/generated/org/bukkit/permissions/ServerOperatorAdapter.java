package dev.meldrite.paper.generated.org.bukkit.permissions;
import dev.meldrite.service.PermissionService;
import java.util.Objects;
public final class ServerOperatorAdapter implements org.bukkit.permissions.ServerOperator {
    private final PermissionService.Subject subject;
    public ServerOperatorAdapter(PermissionService.Subject subject){this.subject=Objects.requireNonNull(subject);}
    public boolean isOp(){return subject.isOperator();}
    public void setOp(boolean value){subject.setOperator(value);}
}
