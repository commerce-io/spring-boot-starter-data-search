package io.commerce.spring.data.search.mongodb;

import io.commerce.spring.data.search.SearchCriteria;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.bson.BsonRegularExpression;
import org.springframework.data.mongodb.core.query.Criteria;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Builder
@RequiredArgsConstructor
public class CriteriaBuilder {

    private static final String REGEX_WITH_OPTION_PATTERN = "^/(.*)/([gimscxdtu]*)$";
    private static final String REGEX_PATTERN = "^/(.*)$";

    private static final Pattern regExpWithOptionPattern;
    private static final Pattern regExpPattern;

    static {
        regExpWithOptionPattern = Pattern.compile(REGEX_WITH_OPTION_PATTERN);
        regExpPattern = Pattern.compile(REGEX_PATTERN);
    }

    private final SearchCriteria searchCriteria;

    public Criteria toCriteria() {
        Criteria criteria = Criteria.where(searchCriteria.getKey());
        switch (searchCriteria.getOp()) {
            case EQ:
                return eq(searchCriteria);
            case NE:
                return ne(searchCriteria);
            case GT:
                return gt(searchCriteria);
            case GE:
                return gte(searchCriteria);
            case LT:
                return lt(searchCriteria);
            case LE:
                return lte(searchCriteria);
            case EXISTS:
                return criteria.exists(searchCriteria.isExists());
            case UNKNOWN:
            default:
                return null;
        }
    }

    private Criteria eq(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return eqCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return eqCriteriaForBooleanValue(searchCriteria);
        }
        return eqCriteria(searchCriteria);
    }

    private Criteria ne(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return neCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return neCriteriaForBooleanValue(searchCriteria);
        }
        return neCriteria(searchCriteria);
    }

    private Criteria gt(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return gtCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return gtCriteriaForBooleanValue(searchCriteria);
        }
        return gtCriteria(searchCriteria);
    }

    private Criteria gte(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return gteCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return gteCriteriaForBooleanValue(searchCriteria);
        }
        return gteCriteria(searchCriteria);
    }

    private Criteria lt(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return ltCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return ltCriteriaForBooleanValue(searchCriteria);
        }
        return ltCriteria(searchCriteria);
    }

    private Criteria lte(SearchCriteria searchCriteria) {
        if (Number.class.equals(searchCriteria.getType())) {
            return lteCriteriaForNumberValue(searchCriteria);
        } else if (Boolean.class.equals(searchCriteria.getType())) {
            return lteCriteriaForBooleanValue(searchCriteria);
        }
        return lteCriteria(searchCriteria);
    }

    private Criteria eqCriteria(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            var values = Stream.of(getValues(searchCriteria.getValue()))
                    .map(this::cleanValue)
                    .toArray();

            return Criteria.where(searchCriteria.getKey()).in(values);
        } else {
            Object value = getValue(searchCriteria.getValue());
            return eq(searchCriteria.getKey(), value);
        }
    }

    private Criteria neCriteria(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            var values = Stream.of(getValues(searchCriteria.getValue()))
                    .map(this::cleanValue)
                    .toArray();

            return Criteria.where(searchCriteria.getKey()).nin(values);
        } else {
            Object value = getValue(searchCriteria.getValue());
            return ne(searchCriteria.getKey(), value);
        }
    }

    private Criteria gtCriteria(SearchCriteria searchCriteria) {
        return Criteria.where(searchCriteria.getKey()).gt(searchCriteria.getValue());
    }

    private Criteria gteCriteria(SearchCriteria searchCriteria) {
        return Criteria.where(searchCriteria.getKey()).gte(searchCriteria.getValue());
    }

    private Criteria ltCriteria(SearchCriteria searchCriteria) {
        return Criteria.where(searchCriteria.getKey()).lt(searchCriteria.getValue());
    }

    private Criteria lteCriteria(SearchCriteria searchCriteria) {
        return Criteria.where(searchCriteria.getKey()).lte(searchCriteria.getValue());
    }

    private Criteria eqCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            String[] values = getValues(searchCriteria.getValue());

            var stringValues = Stream.of(values)
                    .toArray();
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).in(stringValues);

            var booleanValues = Stream.of(values)
                    .map(this::getBooleanValue)
                    .toArray();
            Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).in(booleanValues);
            return new Criteria().orOperator(stringCriteria, booleanCriteria);
        } else {
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).is(searchCriteria.getValue());
            Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).is(getBooleanValue(searchCriteria.getValue()));
            return new Criteria().orOperator(stringCriteria, booleanCriteria);
        }
    }

    private Criteria neCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            String[] values = getValues(searchCriteria.getValue());

            var stringValues = Stream.of(values)
                    .toArray();
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).nin(stringValues);

            var booleanValues = Stream.of(values)
                    .map(this::getBooleanValue)
                    .toArray();
            Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).nin(booleanValues);
            return new Criteria().orOperator(stringCriteria, booleanCriteria);
        } else {
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).ne(searchCriteria.getValue());
            Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).ne(getBooleanValue(searchCriteria.getValue()));
            return new Criteria().orOperator(stringCriteria, booleanCriteria);
        }
    }

    private Criteria gtCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).gt(searchCriteria.getValue());
        Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).gt(getBooleanValue(searchCriteria.getValue()));
        return new Criteria().orOperator(stringCriteria, booleanCriteria);
    }

    private Criteria gteCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).gte(searchCriteria.getValue());
        Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).gte(getBooleanValue(searchCriteria.getValue()));
        return new Criteria().orOperator(stringCriteria, booleanCriteria);
    }

    private Criteria ltCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).lt(searchCriteria.getValue());
        Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).lt(getBooleanValue(searchCriteria.getValue()));
        return new Criteria().orOperator(stringCriteria, booleanCriteria);
    }

    private Criteria lteCriteriaForBooleanValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).lte(searchCriteria.getValue());
        Criteria booleanCriteria = Criteria.where(searchCriteria.getKey()).lte(getBooleanValue(searchCriteria.getValue()));
        return new Criteria().orOperator(stringCriteria, booleanCriteria);
    }

    private Criteria eqCriteriaForNumberValue(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            String[] values = getValues(searchCriteria.getValue());

            var stringValues = Stream.of(values)
                    .toArray();
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).in(stringValues);

            var numberValues = Stream.of(values)
                    .map(this::getNumberValue)
                    .filter(Objects::nonNull)
                    .toArray();
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).in(numberValues);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        } else {
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).is(searchCriteria.getValue());

            Number number = getNumberValue(searchCriteria.getValue());
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).is(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
    }

    private Criteria neCriteriaForNumberValue(SearchCriteria searchCriteria) {
        if (searchCriteria.isArray()) {
            String[] values = getValues(searchCriteria.getValue());

            var stringValues = Stream.of(values)
                    .toArray();
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).nin(stringValues);

            var numberValues = Stream.of(values)
                    .map(this::getNumberValue)
                    .filter(Objects::nonNull)
                    .toArray();
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).nin(numberValues);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        } else {
            Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).ne(searchCriteria.getValue());

            Number number = getNumberValue(searchCriteria.getValue());
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).ne(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
    }

    private Criteria gtCriteriaForNumberValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).gt(searchCriteria.getValue());

        Number number = getNumberValue(searchCriteria.getValue());
        if (number != null) {
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).gt(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
        return stringCriteria;
    }

    private Criteria gteCriteriaForNumberValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).gte(searchCriteria.getValue());

        Number number = getNumberValue(searchCriteria.getValue());
        if (number != null) {
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).gte(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
        return stringCriteria;
    }

    private Criteria ltCriteriaForNumberValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).lt(searchCriteria.getValue());

        Number number = getNumberValue(searchCriteria.getValue());
        if (number != null) {
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).lt(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
        return stringCriteria;
    }

    private Criteria lteCriteriaForNumberValue(SearchCriteria searchCriteria) {
        Criteria stringCriteria = Criteria.where(searchCriteria.getKey()).lte(searchCriteria.getValue());

        Number number = getNumberValue(searchCriteria.getValue());
        if (number != null) {
            Criteria numberCriteria = Criteria.where(searchCriteria.getKey()).lte(number);
            return new Criteria().orOperator(stringCriteria, numberCriteria);
        }
        return stringCriteria;
    }

    private Criteria eq(String key, Object value) {
        Criteria criteria = Criteria.where(key);
        if (value instanceof BsonRegularExpression) {
            criteria.regex((BsonRegularExpression) value);
        } else {
            criteria.is(value);
        }
        return criteria;
    }

    private Criteria ne(String key, Object value) {
        Criteria criteria = Criteria.where(key);
        if (value instanceof BsonRegularExpression) {
            criteria.not().regex((BsonRegularExpression) value);
        } else {
            criteria.ne(value);
        }
        return criteria;
    }

    private String[] getValues(String value) {
        return value.split("(?<!\\\\),");
    }

    private Number getNumberValue(String value) {
        try {
            return NumberFormat.getInstance().parse(value);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private Boolean getBooleanValue(String value) {
        return Boolean.valueOf(value);
    }

    private Object getValue(String value) {
        Matcher matcherRegExpWithOption = regExpWithOptionPattern.matcher(value);
        if (matcherRegExpWithOption.matches()) {
            return new BsonRegularExpression(
                    matcherRegExpWithOption.group(1),
                    matcherRegExpWithOption.group(2));
        }

        Matcher matcherRegExp = regExpPattern.matcher(value);
        if (matcherRegExp.matches()) {
            return new BsonRegularExpression(
                    matcherRegExp.group(1));
        }

        return cleanValue(value);
    }

    private String cleanValue(String value) {
        return value.replace("\\,", ",");
    }
}
