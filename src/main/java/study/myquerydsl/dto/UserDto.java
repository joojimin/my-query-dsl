package study.myquerydsl.dto;

import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(of = {"name", "fakeAge"})
@NoArgsConstructor
public class UserDto {

    private String name;
    private Integer fakeAge;

}
