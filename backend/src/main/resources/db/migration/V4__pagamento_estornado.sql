ALTER TABLE pagamento DROP CONSTRAINT
ck_pagamento_status;
ALTER TABLE pagamento ADD CONSTRAINT
ck_pagamento_status CHECK ( status IN ('PENDENTE', 'APROVADO', 'RECUSADO', 'EXPIRADO', 'ESTORNADO'));
