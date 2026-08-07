public class Cliente {
    protected int id;
    protected String nome;
    protected String endereco;
    protected String telefone;
    protected String cpf;

    public Cliente(int id, String nome, String endereco, String telefone, String cpf) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.telefone = telefone;
        this.cpf = cpf;
    }
}