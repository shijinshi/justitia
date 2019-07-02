package cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity;


import java.util.List;
import java.util.Map;

public class ServerConfigEntity {
    private String version;
    private Integer port;
    private Boolean debug;
    private Integer crlsizelimit;
    private Tls tls;
    private Ca ca;
    private Crl crl;
    private Registry registry;
    private DB db;
    private Ldap ldap;
    private Map affiliations;
    private Map signing;        //存在键名无法用变量标识，直接转成Map
    private Csr csr;
    private Idemix idemix;
    private Map bccsp;          //存在键名无法用变量标识，直接转成Map
    private Integer cacount;
    private List<String> cafiles;
    private Intermediate intermediate;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Integer getCrlsizelimit() {
        return crlsizelimit;
    }

    public void setCrlsizelimit(Integer crlsizelimit) {
        this.crlsizelimit = crlsizelimit;
    }

    public Tls getTls() {
        return tls;
    }

    public void setTls(Tls tls) {
        this.tls = tls;
    }

    public Ca getCa() {
        return ca;
    }

    public void setCa(Ca ca) {
        this.ca = ca;
    }

    public Crl getCrl() {
        return crl;
    }

    public void setCrl(Crl crl) {
        this.crl = crl;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(Ldap ldap) {
        this.ldap = ldap;
    }

    public Map getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(Map affiliations) {
        this.affiliations = affiliations;
    }

    public Map getSigning() {
        return signing;
    }

    public void setSigning(Map signing) {
        this.signing = signing;
    }

    public Csr getCsr() {
        return csr;
    }

    public void setCsr(Csr csr) {
        this.csr = csr;
    }

    public Idemix getIdemix() {
        return idemix;
    }

    public void setIdemix(Idemix idemix) {
        this.idemix = idemix;
    }

    public Map getBccsp() {
        return bccsp;
    }

    public void setBccsp(Map bccsp) {
        this.bccsp = bccsp;
    }

    public Integer getCacount() {
        return cacount;
    }

    public void setCacount(Integer cacount) {
        this.cacount = cacount;
    }

    public List<String> getCafiles() {
        return cafiles;
    }

    public void setCafiles(List<String> cafiles) {
        this.cafiles = cafiles;
    }

    public Intermediate getIntermediate() {
        return intermediate;
    }

    public void setIntermediate(Intermediate intermediate) {
        this.intermediate = intermediate;
    }

    public static class Tls {
        private boolean enabled;
        private String certfile;
        private String keyfile;
        private Clientauth clientauth;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCertfile() {
            return certfile;
        }

        public void setCertfile(String certfile) {
            this.certfile = certfile;
        }

        public String getKeyfile() {
            return keyfile;
        }

        public void setKeyfile(String keyfile) {
            this.keyfile = keyfile;
        }

        public Clientauth getClientauth() {
            return clientauth;
        }

        public void setClientauth(Clientauth clientauth) {
            this.clientauth = clientauth;
        }

        public static class Clientauth {
            private String type;
            private String certfiles;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getCertfiles() {
                return certfiles;
            }

            public void setCertfiles(String certfiles) {
                this.certfiles = certfiles;
            }
        }

    }

    public static class Ca {
        private String name;
        private String keyfile;
        private String certfile;
        private String chainfile;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKeyfile() {
            return keyfile;
        }

        public void setKeyfile(String keyfile) {
            this.keyfile = keyfile;
        }

        public String getCertfile() {
            return certfile;
        }

        public void setCertfile(String certfile) {
            this.certfile = certfile;
        }

        public String getChainfile() {
            return chainfile;
        }

        public void setChainfile(String chainfile) {
            this.chainfile = chainfile;
        }
    }

    public static class Crl {
        private String expiry;

        public String getExpiry() {
            return expiry;
        }

        public void setExpiry(String expiry) {
            this.expiry = expiry;
        }
    }

    public static class Registry {
        private int maxenrollments;
        private List<Identity> identities;

        public int getMaxenrollments() {
            return maxenrollments;
        }

        public void setMaxenrollments(int maxenrollments) {
            this.maxenrollments = maxenrollments;
        }

        public List<Identity> getIdentities() {
            return identities;
        }

        public void setIdentities(List<Identity> identities) {
            this.identities = identities;
        }

        public static class Identity {
            private String name;
            private String pass;
            private String type;
            private String affiliation;
            private Map attrs;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPass() {
                return pass;
            }

            public void setPass(String pass) {
                this.pass = pass;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getAffiliation() {
                return affiliation;
            }

            public void setAffiliation(String affiliation) {
                this.affiliation = affiliation;
            }

            public Map getAttrs() {
                return attrs;
            }

            public void setAttrs(Map attrs) {
                this.attrs = attrs;
            }

            public static class Attrs {
                private String hf_Registrar_Roles;
                private String hf_Registrar_DelegateRoles;
                private boolean hf_Revoker;
                private boolean hf_IntermediateCA;
                private boolean hf_GenCRL;
                private String hf_Registrar_Attributes;
                private boolean hf_AffiliationMgr;

                public String getHf_Registrar_Roles() {
                    return hf_Registrar_Roles;
                }

                public void setHf_Registrar_Roles(String hf_Registrar_Roles) {
                    this.hf_Registrar_Roles = hf_Registrar_Roles;
                }

                public String getHf_Registrar_DelegateRoles() {
                    return hf_Registrar_DelegateRoles;
                }

                public void setHf_Registrar_DelegateRoles(String hf_Registrar_DelegateRoles) {
                    this.hf_Registrar_DelegateRoles = hf_Registrar_DelegateRoles;
                }

                public boolean isHf_Revoker() {
                    return hf_Revoker;
                }

                public void setHf_Revoker(boolean hf_Revoker) {
                    this.hf_Revoker = hf_Revoker;
                }

                public boolean isHf_IntermediateCA() {
                    return hf_IntermediateCA;
                }

                public void setHf_IntermediateCA(boolean hf_IntermediateCA) {
                    this.hf_IntermediateCA = hf_IntermediateCA;
                }

                public boolean isHf_GenCRL() {
                    return hf_GenCRL;
                }

                public void setHf_GenCRL(boolean hf_GenCRL) {
                    this.hf_GenCRL = hf_GenCRL;
                }

                public String getHf_Registrar_Attributes() {
                    return hf_Registrar_Attributes;
                }

                public void setHf_Registrar_Attributes(String hf_Registrar_Attributes) {
                    this.hf_Registrar_Attributes = hf_Registrar_Attributes;
                }

                public boolean isHf_AffiliationMgr() {
                    return hf_AffiliationMgr;
                }

                public void setHf_AffiliationMgr(boolean hf_AffiliationMgr) {
                    this.hf_AffiliationMgr = hf_AffiliationMgr;
                }
            }
        }
    }

    public static class DB {
        private String type;
        private String datasource;
        private Tls tls;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDatasource() {
            return datasource;
        }

        public void setDatasource(String datasource) {
            this.datasource = datasource;
        }

        public Tls getTls() {
            return tls;
        }

        public void setTls(Tls tls) {
            this.tls = tls;
        }

        public static class Tls {
            private boolean enabled;
            private List<String> certfiles;
            private Client client;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public List<String> getCertfiles() {
                return certfiles;
            }

            public void setCertfiles(List<String> certfiles) {
                this.certfiles = certfiles;
            }

            public Client getClient() {
                return client;
            }

            public void setClient(Client client) {
                this.client = client;
            }

            public static class Client {
                private String certfile;
                private String keyfile;

                public String getCertfile() {
                    return certfile;
                }

                public void setCertfile(String certfile) {
                    this.certfile = certfile;
                }

                public String getKeyfile() {
                    return keyfile;
                }

                public void setKeyfile(String keyfile) {
                    this.keyfile = keyfile;
                }
            }
        }

    }

    public static class Ldap {
        private Boolean enabled;
        private String url;
        private Tls tls;
        private Attribute attribute;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Tls getTls() {
            return tls;
        }

        public void setTls(Tls tls) {
            this.tls = tls;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public void setAttribute(Attribute attribute) {
            this.attribute = attribute;
        }

        public static class Tls {
            private List<String> certfiles;
            private Client client;

            public List<String> getCertfiles() {
                return certfiles;
            }

            public void setCertfiles(List<String> certfiles) {
                this.certfiles = certfiles;
            }

            public Client getClient() {
                return client;
            }

            public void setClient(Client client) {
                this.client = client;
            }

            public static class Client {
                private String certfile;
                private String keyfile;

                public String getCertfile() {
                    return certfile;
                }

                public void setCertfile(String certfile) {
                    this.certfile = certfile;
                }

                public String getKeyfile() {
                    return keyfile;
                }

                public void setKeyfile(String keyfile) {
                    this.keyfile = keyfile;
                }
            }
        }

        public static class Attribute {
            private List<String> names;
            private List<Converter> converters;
            private Maps maps;

            public List<String> getNames() {
                return names;
            }

            public void setNames(List<String> names) {
                this.names = names;
            }

            public List<Converter> getConverters() {
                return converters;
            }

            public void setConverters(List<Converter> converters) {
                this.converters = converters;
            }

            public Maps getMaps() {
                return maps;
            }

            public void setMaps(Maps maps) {
                this.maps = maps;
            }

            public static class Converter {
                private String name;
                private String value;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class Maps {
                private List<Groups> groups;

                public List<Groups> getGroups() {
                    return groups;
                }

                public void setGroups(List<Groups> groups) {
                    this.groups = groups;
                }

                public static class Groups{
                    private String name;
                    private String value;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }
                }
            }

        }
    }

    public static class Affiliations {
        private Map affiliations;

        public Map getAffiliations() {
            return affiliations;
        }

        public void setAffiliations(Map affiliations) {
            this.affiliations = affiliations;
        }
    }

    public static class Signing {
        private Default default1;
        private Profiles profiles;


        public Default getDefault1() {
            return default1;
        }

        public void setDefault1(Default default1) {
            this.default1 = default1;
        }

        public Profiles getProfiles() {
            return profiles;
        }

        public void setProfiles(Profiles profiles) {
            this.profiles = profiles;
        }

        public static class Default{
            private List<String> usage;
            private String expiry;

            public List<String> getUsage() {
                return usage;
            }

            public void setUsage(List<String> usage) {
                this.usage = usage;
            }

            public String getExpiry() {
                return expiry;
            }

            public void setExpiry(String expiry) {
                this.expiry = expiry;
            }
        }

        public static class Profiles{
            private Ca ca;
            private Tls tls;

            public Ca getCa() {
                return ca;
            }

            public void setCa(Ca ca) {
                this.ca = ca;
            }

            public Tls getTls() {
                return tls;
            }

            public void setTls(Tls tls) {
                this.tls = tls;
            }

            public static class Ca{
                private List<String> usage;
                private String expiry;
                private Caconstraint caconstraint;

                public List<String> getUsage() {
                    return usage;
                }

                public void setUsage(List<String> usage) {
                    this.usage = usage;
                }

                public String getExpiry() {
                    return expiry;
                }

                public void setExpiry(String expiry) {
                    this.expiry = expiry;
                }

                public Caconstraint getCaconstraint() {
                    return caconstraint;
                }

                public void setCaconstraint(Caconstraint caconstraint) {
                    this.caconstraint = caconstraint;
                }

                public static class Caconstraint{
                    private Boolean isca;
                    private Integer maxpathlen;

                    public Boolean getIsca() {
                        return isca;
                    }

                    public void setIsca(Boolean isca) {
                        this.isca = isca;
                    }

                    public Integer getMaxpathlen() {
                        return maxpathlen;
                    }

                    public void setMaxpathlen(Integer maxpathlen) {
                        this.maxpathlen = maxpathlen;
                    }
                }
            }

            public static class Tls{
                private List<String> usage;
                private String expiry;

                public List<String> getUsage() {
                    return usage;
                }

                public void setUsage(List<String> usage) {
                    this.usage = usage;
                }

                public String getExpiry() {
                    return expiry;
                }

                public void setExpiry(String expiry) {
                    this.expiry = expiry;
                }
            }
        }
    }

    public static class Csr {
        private String cn;
        private Keyrequest keyrequest;
        private List<Map<String,String>> names;
        private List<String> hosts;
        private Ca ca;

        public String getCn() {
            return cn;
        }

        public void setCn(String cn) {
            this.cn = cn;
        }

        public Keyrequest getKeyrequest() {
            return keyrequest;
        }

        public void setKeyrequest(Keyrequest keyrequest) {
            this.keyrequest = keyrequest;
        }

        public List<Map<String, String>> getNames() {
            return names;
        }

        public void setNames(List<Map<String, String>> names) {
            this.names = names;
        }

        public List<String> getHosts() {
            return hosts;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        public Ca getCa() {
            return ca;
        }

        public void setCa(Ca ca) {
            this.ca = ca;
        }

        public static class Keyrequest{
            private String algo;
            private Integer size;

            public String getAlgo() {
                return algo;
            }

            public void setAlgo(String algo) {
                this.algo = algo;
            }

            public Integer getSize() {
                return size;
            }

            public void setSize(Integer size) {
                this.size = size;
            }
        }

        public static class Names{
            private String C;
            private String ST;
            private String L;
            private String O;
            private String OU;

            public String getC() {
                return C;
            }

            public void setC(String C) {
                this.C = C;
            }

            public String getST() {
                return ST;
            }

            public void setST(String ST) {
                this.ST = ST;
            }

            public String getL() {
                return L;
            }

            public void setL(String l) {
                L = l;
            }

            public String getO() {
                return O;
            }

            public void setO(String O) {
                this.O = O;
            }

            public String getOU() {
                return OU;
            }

            public void setOU(String OU) {
                this.OU = OU;
            }
        }

        public static class Ca{
            private String expiry;
            private Integer pathlength;

            public String getExpiry() {
                return expiry;
            }

            public void setExpiry(String expiry) {
                this.expiry = expiry;
            }

            public Integer getPathlength() {
                return pathlength;
            }

            public void setPathlength(Integer pathlength) {
                this.pathlength = pathlength;
            }
        }

    }

    public static class Idemix {
        private Integer rhpoolsize;
        private String nonceexpiration;
        private String noncesweepinterval;

        public Integer getRhpoolsize() {
            return rhpoolsize;
        }

        public void setRhpoolsize(Integer rhpoolsize) {
            this.rhpoolsize = rhpoolsize;
        }

        public String getNonceexpiration() {
            return nonceexpiration;
        }

        public void setNonceexpiration(String nonceexpiration) {
            this.nonceexpiration = nonceexpiration;
        }

        public String getNoncesweepinterval() {
            return noncesweepinterval;
        }

        public void setNoncesweepinterval(String noncesweepinterval) {
            this.noncesweepinterval = noncesweepinterval;
        }
    }

    public static class Bccsp {
        private String default1;
        private Sw sw;

        public String getDefault1() {
            return default1;
        }

        public void setDefault1(String default1) {
            this.default1 = default1;
        }

        public Sw getSw() {
            return sw;
        }

        public void setSw(Sw sw) {
            this.sw = sw;
        }

        public static class Sw{
            private String hash;
            private Integer security;
            private Filekeystore filekeystore;

            public String getHash() {
                return hash;
            }

            public void setHash(String hash) {
                this.hash = hash;
            }

            public Integer getSecurity() {
                return security;
            }

            public void setSecurity(Integer security) {
                this.security = security;
            }

            public Filekeystore getFilekeystore() {
                return filekeystore;
            }

            public void setFilekeystore(Filekeystore filekeystore) {
                this.filekeystore = filekeystore;
            }

            public static class Filekeystore{
                private String filekeystore;

                public String getFilekeystore() {
                    return filekeystore;
                }

                public void setFilekeystore(String filekeystore) {
                    this.filekeystore = filekeystore;
                }
            }
        }
    }

    public static class Intermediate {
        private ParentServer parentserver;
        private Enrollment enrollment;
        private Tls tls;


        public ParentServer getParentserver() {
            return parentserver;
        }

        public void setParentserver(ParentServer parentserver) {
            this.parentserver = parentserver;
        }

        public Enrollment getEnrollment() {
            return enrollment;
        }

        public void setEnrollment(Enrollment enrollment) {
            this.enrollment = enrollment;
        }

        public Tls getTls() {
            return tls;
        }

        public void setTls(Tls tls) {
            this.tls = tls;
        }

        public static class ParentServer {
            private String url;
            private String caname;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getCaname() {
                return caname;
            }

            public void setCaname(String caname) {
                this.caname = caname;
            }
        }

        public static class Enrollment {
            private List<String> hosts;
            private String profile;
            private String label;

            public List<String> getHosts() {
                return hosts;
            }

            public void setHosts(List<String> hosts) {
                this.hosts = hosts;
            }

            public String getProfile() {
                return profile;
            }

            public void setProfile(String profile) {
                this.profile = profile;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }
        }

        public static class Tls {
            private List<String> certfiles;
            private Client client;

            public List<String> getCertfiles() {
                return certfiles;
            }

            public void setCertfiles(List<String> certfiles) {
                this.certfiles = certfiles;
            }

            public Client getClient() {
                return client;
            }

            public void setClient(Client client) {
                this.client = client;
            }

            public static class Client {
                private String certfile;
                private String keyfile;

                public String getCertfile() {
                    return certfile;
                }

                public void setCertfile(String certfile) {
                    this.certfile = certfile;
                }

                public String getKeyfile() {
                    return keyfile;
                }

                public void setKeyfile(String keyfile) {
                    this.keyfile = keyfile;
                }
            }
        }
    }
}
