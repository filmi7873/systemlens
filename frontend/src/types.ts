export type ArchitectureNodeType =
  | "service"
  | "database"
  | "frontend"
  | "queue"
  | "worker"
  | "external";

export type DataSensitivity =
  | "none"
  | "internal"
  | "confidential"
  | "restricted";

export type ComplianceTag =
  | "PII"
  | "PCI"
  | "HIPAA"
  | "SOC2"
  | "GDPR";

export type ArchitectureNode = {
  id: string;
  label: string;
  type: ArchitectureNodeType | string;

  containsPii: boolean;
  dataSensitivity: DataSensitivity;
  complianceTags: ComplianceTag[];
};

export type ArchitectureEdge = {
  id: string;
  sourceNode: string;
  targetNode: string;
  relationship: string;
};

export type SimulationResultResponse = {
  failedNode?: string;
  changedNode?: string;
  severity: string;
  directlyAffected: string[];
  indirectlyAffected: string[];
  unaffected: string[];
  impactPaths: string[][];
  explanation: string;
};

export type RiskAssessmentResponse = {
  riskScore: number;
  riskLevel: string;
  riskFactors: string[];
  recommendations: string[];
};

export type SimulationAnalysisResponse = {
  simulation: SimulationResultResponse;
  riskAssessment: RiskAssessmentResponse;
};

export type CustomOutageSimulationRequest = {
  nodes: ArchitectureNode[];
  edges: ArchitectureEdge[];
  failedNode: string;
};

export type CustomSchemaChangeSimulationRequest = {
  nodes: ArchitectureNode[];
  edges: ArchitectureEdge[];
  changedNode: string;
};