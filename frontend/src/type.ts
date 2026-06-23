export type ArchitectureNode = {
  id: string;
  label: string;
  type: string;
};

export type ArchitectureEdge = {
  id: string;
  sourceNode: string;
  targetNode: string;
  relationship: string;
};

export type SimulationResultResponse = {
  failedNode: string;
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