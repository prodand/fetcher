export class Case {
  id: string;
  number: string;
  status: string;
  description: string;
  when: Date;
  lastUpdate: string;
  previousStatus: string;
  history: History;
}

export class History {
  when: Date;
  info: string;
}
