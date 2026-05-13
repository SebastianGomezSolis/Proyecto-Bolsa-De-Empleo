package una.sistema.proyectobolsaempleobackend.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import una.sistema.proyectobolsaempleobackend.logic.servicios.*;

@Component
public class ModeloDatos {
    @Autowired private AuthService authService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private AdministradorService administradorService;
    @Autowired private EmpresaService empresaService;
    @Autowired private OferenteService oferenteService;
    @Autowired private NacionalidadService nacionalidadService;
    @Autowired private CaracteristicaService caracteristicaService;
    @Autowired private HabilidadService habilidadService;
    @Autowired private PuestoService puestoService;
    @Autowired private PuestoCaracteristicaService puestoCaracteristicaService;
    @Autowired private MatchingService matchingService;
    @Autowired private ReporteService reporteService;
    @Autowired private TipoCambioServicio tipoCambioServicio;

    public AuthService getAuthService(){ return authService; }
    public UsuarioService getUsuarioService() { return usuarioService; }
    public AdministradorService getAdministradorService() { return administradorService; }
    public EmpresaService getEmpresaService() { return empresaService; }
    public OferenteService getOferenteService() { return oferenteService; }
    public NacionalidadService getNacionalidadService() { return nacionalidadService; }
    public CaracteristicaService getCaracteristicaService() { return caracteristicaService; }
    public HabilidadService getHabilidadService() { return habilidadService; }
    public PuestoService getPuestoService() { return puestoService; }
    public PuestoCaracteristicaService getPuestoCaracteristicaService() { return puestoCaracteristicaService; }
    public MatchingService getMatchingService() { return matchingService; }
    public ReporteService getReporteService() { return reporteService; }
    public TipoCambioServicio getTipoCambioServicio() { return tipoCambioServicio; }
}
