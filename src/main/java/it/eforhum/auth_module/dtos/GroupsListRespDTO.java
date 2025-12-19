package it.eforhum.auth_module.dtos;

public record GroupsListRespDTO(String[] groups){

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupsListRespDTO that = (GroupsListRespDTO) o;

        return java.util.Arrays.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(groups);
    }

    @Override
    public String toString() {
        return "GroupsListRespDTO{" +
                "groups=" + java.util.Arrays.toString(groups) +
                '}';
    }
    
}
