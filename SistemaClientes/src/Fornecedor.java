public class Fornecedor {
    protected int id;
    protected String nome;
    protected String telefone;
    protected String cnpj;

    public Fornecedor(int id, String nome, String telefone, String cnpj) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.cnpj = cnpj;
    }
}